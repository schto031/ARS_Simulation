package main.java;
import main.java.ai.DefaultGeneDestroyer;
import main.java.ai.NeuralNetwork;
import main.java.ai.RecurrentNeuralNetwork;
import main.java.ai.RobotController;
import main.java.common.Arena;
import main.java.common.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Runner extends JFrame {
    private static final byte NUMBER_OF_ROBOTS=15;
    // Set a threshold above which NN is triggered
    private static final double NN_THRESHOLD=10;

    private static final byte NUMBER_OF_REPRODUCERS= (byte) Math.min(NUMBER_OF_ROBOTS-4,4);
    // Get a handle to the NN threads
    private static List<ScheduledFuture<?>> controllerHandle;
    
    //33% of robots should mutate and crossover
    private static final int MUTATION_NUMBER= (int)(NUMBER_OF_ROBOTS*33/100);
    private static final int CROSSOVER_NUMBER= (int)(NUMBER_OF_ROBOTS*33/100);
    
    public static int NUMBER_OF_GENERATION = 0;

    private static class RoboPanel extends JPanel{
        private Robo[] robots;
        private RobotController[] controllers;
        private List<Line2D> obstacles;
        private Point2D[] dust=new Point2D[8000];
        private ScheduledThreadPoolExecutor executor;

        //Written by Swapneel + Tom
        public RoboPanel(Robo... robo) {
            this.robots=robo;
            setPreferredSize(new Dimension(800,600));
            obstacles= Arena.getBoxedArena(getBounds());
            Arrays.stream(robots).forEach(r-> {
                // Set all obstacles
                r.setObstacles(obstacles);
                // Initalize robots with random velocities
                r.setVelocity((_vl)->Math.random()-0.5, (_vr)->Math.random()-0.5);
                // Set dust (optional)
                r.setAllDust(dust);
            });
            // Initialize a scheduler
            executor=new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
            // Robots position calculation thread
            Arrays.stream(robo).forEach(r->executor.scheduleAtFixedRate(r,0,8, TimeUnit.MILLISECONDS));             // Each robot on a different thread
//            executor.scheduleAtFixedRate(()->Arrays.stream(robo).forEach(Robo::run),0,8, TimeUnit.MILLISECONDS);    // All robots on single thread

            
            // Generation thread
            executor.scheduleWithFixedDelay(()-> nextGen(),15,15,TimeUnit.SECONDS);
            // Display thread
            executor.scheduleAtFixedRate(()->SwingUtilities.invokeLater(this::repaint),0,8, TimeUnit.MILLISECONDS);
            // Printing thread
            executor.scheduleWithFixedDelay(()-> System.out.println(Arrays.toString(robo)),1,1, TimeUnit.SECONDS);
            // Initialize neural network for every bot
            controllers=new RobotController[robots.length];
            initializeNeuralNetwork();
            initializeNeuralNetworkControlThread();
            // Controller debug thread
            executor.scheduleWithFixedDelay(()-> System.err.println(Arrays.toString(controllers[0].getOutput())),1,1, TimeUnit.SECONDS);

            addMouseMotionListener(new MouseMotionListener() {
                @Override
                public void mouseDragged(MouseEvent mouseEvent) { }

                @Override
                public void mouseMoved(MouseEvent mouseEvent) { System.out.println(mouseEvent.getLocationOnScreen()); }
            });
            var panel=new DebugPanel(controllers);
            add(panel);
        }

        @Override
        //Written by Swapneel + Tom
        public void paint(Graphics g) {
            super.paint(g);
            var graphics=(Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Arrays.stream(robots).forEach(r->r.draw(graphics));
            obstacles.forEach(graphics::draw);
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
            for(var i=0;i<dust.length;i++){ dust[i]=Utilities.rand(rand, r); }
        }

        private void initializeNeuralNetwork(){
            for(var i=0;i<controllers.length;i++){
//                var nn=new NeuralNetwork(12,4,2);
                var nn=new RecurrentNeuralNetwork(1, 100, new int[]{12,8,4,2});
                controllers[i]=nn;
                robots[i].inputLayerOfNN=nn.setInputByReference(robots[i].proximitySensors);   // hook up proximity sensors to input of nn
            }
        }

        public void initializeNeuralNetworkControlThread(){
            // NN control thread
            controllerHandle=Arrays.stream(robots).map(r->executor.scheduleWithFixedDelay(()->{
                var controller=controllers[r.getId()];
                controller.forwardPropagate();
                var outputs=controller.getOutput();
                if(outputs[0]>NN_THRESHOLD) r.incrementLeftVelocity();
                else r.decrementLeftVelocity();
                if(outputs[1]>NN_THRESHOLD) r.incrementRightVelocity();
                else r.decrementRightVelocity();
            },1500,8, TimeUnit.MILLISECONDS)).collect(Collectors.toUnmodifiableList());
        }

        public void breed() throws CloneNotSupportedException {
            System.err.println("Breeding a better generation!");
            var random=new Random(System.currentTimeMillis());
            int rand1;
            int rand2;
            RobotController rc1;
            RobotController rc2;
            var winners=Evaluation.getBestBots(robots,NUMBER_OF_REPRODUCERS)
                    .stream()
                    .mapToInt(Robo::getId)
                    .mapToObj(id->controllers[id])
                    .collect(Collectors.toUnmodifiableList());
            var gd=new DefaultGeneDestroyer();
            // mutate/crossover logic goes here
            for(var i=0;i<MUTATION_NUMBER;i++){
                controllers[i]=winners.get(i%winners.size()).clone();
                gd.mutate(controllers[i]);
            }
            for(var i=NUMBER_OF_ROBOTS-1;i>=NUMBER_OF_ROBOTS-CROSSOVER_NUMBER;i--) {
            	rand1 = random.nextInt(NUMBER_OF_REPRODUCERS);
            	rc1= winners.get(rand1).clone();
            	rand2 = random.nextInt(NUMBER_OF_REPRODUCERS-1);
            	//we dont want the same controllers to crossover
            	if(rand1<=rand2) {
            		rand2+=1;
            	}
            	rc2= winners.get(rand2).clone();
            	gd.crossover(rc1, rc2);
            	controllers[i] = rc1.clone();
            }
            for(var i=MUTATION_NUMBER;i<NUMBER_OF_ROBOTS-CROSSOVER_NUMBER;i++) {
            	controllers[i]=winners.get(i%winners.size()).clone();
            }
        }

        public RobotController[] getControllers() { return controllers; }
        
        public void nextGen() {
        	controllerHandle.forEach(h->h.cancel(true));
        	try {
        		for(var r:this.robots) r.getCollectedDustSet().clear();
        		this.breed();
        		this.initializeNeuralNetworkControlThread();
        		NUMBER_OF_GENERATION++;
        	 } catch (CloneNotSupportedException e) {
        		 e.printStackTrace();
        	 }
        	Arrays.stream(this.robots).forEach(b->b.setPosition(super.getBounds().getCenterX(),super.getBounds().getCenterY()));
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
				/*
				 * case 'e': controllerHandle.forEach(h->h.cancel(true)); try { for(var
				 * r:roboPanel.robots) r.getCollectedDustSet().clear(); roboPanel.breed();
				 * roboPanel.initializeNeuralNetworkControlThread(); } catch
				 * (CloneNotSupportedException e) { e.printStackTrace(); }
				 */
                    case 'r': Arrays.stream(bots).forEach(b->b.setPosition(bounds.getCenterX(),bounds.getCenterY()));  break;
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
        getRootPane().registerKeyboardAction(e->System.exit(0), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "true");
        var runner=new Runner();
        SwingUtilities.invokeLater(()->runner.setVisible(true));
    }
}
