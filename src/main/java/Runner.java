import ai.*;
import common.Arena;
import common.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Runner extends JFrame {
    private static final byte NUMBER_OF_ROBOTS=26;
    // Set a threshold above which NN is triggered
    private static final double NN_THRESHOLD=10;

    private static final byte NUMBER_OF_REPRODUCERS= (byte) Math.min(NUMBER_OF_ROBOTS-6,6);
    // Get a handle to the NN threads
    private static List<ScheduledFuture<?>> controllerHandle;
    
    //33% of robots should mutate and crossover
    private static final int MUTATION_NUMBER= NUMBER_OF_ROBOTS*20/100;
    private static final int CROSSOVER_NUMBER= NUMBER_OF_ROBOTS*20/100;

    private static final int DELAY=8;
    private static final int GEN=20;
    protected static AtomicInteger NUMBER_OF_GENERATION = new AtomicInteger();

    private static class RoboPanel extends JPanel{
        private Robo[] robots;
        private final List<Robo> bestRobots=new ArrayList<>();
        private RobotController[] controllers;
        private Arena arena=Arena.getBoxedArena(getBounds());
        private Point2D[] dust=new Point2D[8000];
        private ScheduledThreadPoolExecutor executor;

        //Written by Swapneel + Tom
        public RoboPanel(Robo... robo) {
            this.robots=robo;
            setPreferredSize(new Dimension(800,600));
            Arrays.stream(robots).forEach(r-> {
                // Set all obstacles
                r.setObstacles(arena.getObstacles());
                // Initalize robots with random velocities
                r.setVelocity((_vl)->Math.random()-0.5, (_vr)->Math.random()-0.5);
                // Set dust (optional)
                r.setAllDust(dust);
                // Set initial location
                r.setPosition(arena.getInitialLocation().x, arena.getInitialLocation().y);
            });
            // Initialize a scheduler
            executor=new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
            // Robots position calculation thread
            Arrays.stream(robo).forEach(r->executor.scheduleAtFixedRate(r,0,DELAY, TimeUnit.MILLISECONDS));             // Each robot on a different thread
            // Generation thread
            executor.scheduleWithFixedDelay(this::nextGen,GEN,GEN,TimeUnit.SECONDS);
            // Display thread
            executor.scheduleAtFixedRate(()->SwingUtilities.invokeLater(this::repaint),0,DELAY, TimeUnit.MILLISECONDS);
            // Printing thread
            executor.scheduleWithFixedDelay(()-> System.out.println(Arrays.toString(robo)),1,1, TimeUnit.SECONDS);
            // Initialize neural network for every bot
            controllers=new RobotController[robots.length];
            initializeNeuralNetwork();
            initializeNeuralNetworkControlThread();
            // Controller debug thread
            executor.scheduleWithFixedDelay(()-> System.err.println(Arrays.toString(controllers[0].getOutput())),1,1, TimeUnit.SECONDS);
            executor.scheduleWithFixedDelay(()-> System.err.println(Arrays.toString(controllers[0].getInput())) ,1,1, TimeUnit.SECONDS);
            // Best robots calculation thread
            executor.scheduleWithFixedDelay(()-> {
                bestRobots.clear();
                bestRobots.addAll(Evaluation.getBestBots(robots, NUMBER_OF_REPRODUCERS));
                },1,1, TimeUnit.SECONDS);

            addMouseMotionListener(new MouseMotionListener() {
                @Override
                public void mouseDragged(MouseEvent mouseEvent) { }

                @Override
                public void mouseMoved(MouseEvent mouseEvent) { System.out.println(mouseEvent.getLocationOnScreen()); }
            });
            var panel=new DebugPanel(controllers, bestRobots, NUMBER_OF_GENERATION);
            add(panel);
        }

        @Override
        //Written by Swapneel + Tom
        public void paint(Graphics g) {
            super.paint(g);
            var graphics=(Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Arrays.stream(robots).forEach(r->r.draw(graphics));
            arena.getObstacles().forEach(graphics::draw);
            Arrays.stream(dust).filter(Objects::nonNull).forEach(d->{
                // No method to directly draw a point?! Another of java's quirks
                var p=new Line2D.Double(d,d);
                graphics.setColor(Color.LIGHT_GRAY);
                graphics.draw(p);
            });
        }

        @Override
        public void setPreferredSize(Dimension preferredSize) {
            super.setPreferredSize(preferredSize);
            var rand=new Random();
            var r=new Rectangle(preferredSize);
            for(var i=0;i<dust.length;i++){ dust[i]= Utilities.rand(rand, r); }
        }

        private void initializeNeuralNetwork(){

                for(var i=0;i<controllers.length;i++){
//                var nn=new NeuralNetwork(12,4,2);
                    var nn=new RecurrentNeuralNetwork(1, 10, new RobotController.ClippedRelu(NN_THRESHOLD*2), 12,4,2);
                    controllers[i]=nn;
//                robots[i].inputLayerOfNN=nn.setInputByReference(robots[i].proximitySensors);   // hook up proximity sensors to input of nn
            }
        }

        public void initializeNeuralNetworkControlThread(){
            // NN control thread
            controllerHandle=Arrays.stream(robots).map(r->executor.scheduleWithFixedDelay(()->{
                try{
                    var controller=controllers[r.getId()];
                    controller.setInputByValue(r.proximitySensors);
                    controller.forwardPropagate();
                    var outputs=controller.getOutput();
                    if(outputs[0]>NN_THRESHOLD) r.incrementLeftVelocity();
                    else r.decrementLeftVelocity();
                    if(outputs[1]>NN_THRESHOLD) r.incrementRightVelocity();
                    else r.decrementRightVelocity();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            },1500,DELAY*4, TimeUnit.MILLISECONDS)).collect(Collectors.toUnmodifiableList());
        }

        public void breed() throws CloneNotSupportedException {
            System.err.println("Breeding a better generation!");
            var random=new Random(System.currentTimeMillis());
            bestRobots.clear();
            bestRobots.addAll(Evaluation.getBestBots(robots, NUMBER_OF_REPRODUCERS));
            var winners=bestRobots
                    .stream()
                    .mapToInt(Robo::getId)
                    .mapToObj(id->controllers[id])
                    .collect(Collectors.toUnmodifiableList());
            for(var b:bestRobots){ System.err.println(Evaluation.evaluationFunction(b)+" "+b); }
            var gd=new DefaultGeneDestroyer();
//            var gd=new SinglePointGeneDestroyer();
            // mutate/crossover logic goes here
            for(var i=0;i<NUMBER_OF_ROBOTS;i++){ controllers[i]=winners.get(i%winners.size()).clone(); }
            for(var i=0;i<NUMBER_OF_ROBOTS;i++){
                int r1, r2;
                do{
                    r1=random.nextInt(NUMBER_OF_ROBOTS);
                    r2=random.nextInt(NUMBER_OF_ROBOTS);
                } while (r1==r2 && controllers[r1].ID==controllers[r2].ID);
                System.err.println("Crossover "+r1+" and "+r2);
                gd.crossover(controllers[r1], controllers[r2]);
            }
            for (var controller : controllers) { controller.ID = UUID.randomUUID(); }
            for(var i=0;i<NUMBER_OF_ROBOTS;i++){
                if(random.nextBoolean()) continue;
                System.err.println("Mutate "+i);
                gd.mutate(controllers[i]);
            }
        }

        public RobotController[] getControllers() { return controllers; }
        
        public void nextGen() {
        	controllerHandle.forEach(h->h.cancel(true));
            controllerHandle.forEach(Future::isDone);
            var evals=Arrays.stream(robots).map(Evaluation::evaluationFunction).collect(Collectors.toUnmodifiableList());
            var min=evals.stream().mapToDouble(d->d).min().orElse(0);
            var max=evals.stream().mapToDouble(d->d).max().orElse(0);
            var avg=evals.stream().mapToDouble(d->d).average().orElse(0);
            try(var fw=new FileWriter(System.getProperty("METRICS_LOCATION", "metrics.csv"), true); var ps=new PrintWriter(fw)){
                ps.printf("%d,%f,%f,%f",NUMBER_OF_GENERATION.get(),min,max,avg);
                ps.println();
            } catch (IOException e){ e.printStackTrace(); }
            try {
        		this.breed();
                for(var r:this.robots) {
                    r.resetParameters();
                    r.setVelocity((_vl)->Math.random()-0.5, (_vr)->Math.random()-0.5);
                }
                this.initializeNeuralNetworkControlThread();
        		NUMBER_OF_GENERATION.incrementAndGet();
        	 } catch (CloneNotSupportedException e) {
        		 e.printStackTrace();
        	 }
        	Arrays.stream(this.robots).forEach(b->b.setPosition(arena.getInitialLocation().x, arena.getInitialLocation().y));
        }
    }
    //Written by Swapneel
    private Runner() {
        setTitle("Low budget robot simulator");
        var menu=new JMenu("Options");
        var menuBar=new JMenuBar();
        var reconfigure=new JMenuItem("Reconfigure");
        reconfigure.addActionListener(a-> JOptionPane.showMessageDialog(this, "This does nothing yet"));
        menu.add(reconfigure);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        // Initialize robots
        var bots=new Robo[NUMBER_OF_ROBOTS];
        for(var i=0;i<NUMBER_OF_ROBOTS;i++){
            var robo=new Robo(36, null, i);
            bots[i]=robo;
        }
        var robo=bots[0];
        var roboPanel=new RoboPanel(bots);
        add(roboPanel);
        pack();
        
        var bounds=getBounds();
        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) { }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                switch (keyEvent.getKeyChar()){
                    case 'w': robo.incrementRightVelocity(); break;
                    case 's': robo.decrementRightVelocity(); break;
                    case 'o': robo.incrementLeftVelocity(); break;
                    case 'l': robo.decrementLeftVelocity(); break;
                    case 'x': robo.stop(); break;
                    case 'e': roboPanel.nextGen();
                    case 'r': Arrays.stream(bots).forEach(b->b.setPosition(roboPanel.arena.getInitialLocation().x, roboPanel.arena.getInitialLocation().y)); break;
                    case 't': robo.incrementBothVelocity(); break;
                    case 'g': robo.decrementBothVelocity();  break;
                    case 'b': robo.setPosition(bounds.getCenterX(),bounds.getCenterY()); break;
                    default: for(var c:roboPanel.controllers) {
                        try {
                            c.toFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) { }
        });
        getRootPane().registerKeyboardAction(e->{

            try(var fos=new FileOutputStream(System.getProperty("TRAINED","weights.obj"));
                var oos=new ObjectOutputStream(fos)) {
                System.err.println("Dumping weights!");
                oos.writeObject(roboPanel.controllers);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.exit(0);
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "true");
        try(var fw=new FileWriter(System.getProperty("METRICS_LOCATION", "metrics.csv")); var ps=new PrintWriter(fw)){
            ps.println("Generation,Min,Max,Average");
        } catch (IOException e){ e.printStackTrace(); }
        var runner=new Runner();
        SwingUtilities.invokeLater(()->runner.setVisible(true));
    }
}
