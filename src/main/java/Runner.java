import ai.DefaultGeneDestroyer;
import ai.IRobotController;
import ai.RecurrentNeuralNetwork;
import ai.RobotController;
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Runner extends JFrame {
    private static final byte NUMBER_OF_ROBOTS=16;
    // Set a threshold above which NN is triggered
    private static final double NN_THRESHOLD=10;

    private static final byte NUMBER_OF_REPRODUCERS= (byte) Math.min(NUMBER_OF_ROBOTS-4,4);
    // Get a handle to the NN threads
    private static List<ScheduledFuture<?>> controllerHandle;

    private static class RoboPanel extends JPanel{
        private Robo[] robots;
        private RobotController[] controllers;
        private List<Line2D> obstacles;
        private Point2D[] dust=new Point2D[8000];

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
            var executor=new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
            // Robots position calculation thread
            Arrays.stream(robo).forEach(r->executor.scheduleAtFixedRate(r,0,8, TimeUnit.MILLISECONDS));             // Each robot on a different thread
//            executor.scheduleAtFixedRate(()->Arrays.stream(robo).forEach(Robo::run),0,8, TimeUnit.MILLISECONDS);    // All robots on single thread

            // Display thread
            executor.scheduleAtFixedRate(()->SwingUtilities.invokeLater(this::repaint),0,8, TimeUnit.MILLISECONDS);
            // Printing thread
            executor.scheduleWithFixedDelay(()-> System.out.println(Arrays.toString(robo)),1,1, TimeUnit.SECONDS);
            // Initialize neural network for every bot
            controllers=new RobotController[robots.length];
            initializeNeuralNetwork();
            // Controller debug thread
            executor.scheduleWithFixedDelay(()-> System.err.println(Arrays.toString(controllers[0].getOutput())),1,1, TimeUnit.SECONDS);
            // NN control thread
            controllerHandle=Arrays.stream(robo).map(r->executor.scheduleWithFixedDelay(()->{
                var controller=controllers[r.getId()];
                controller.forwardPropagate();
                var outputs=controller.getOutput();
                if(outputs[0]>NN_THRESHOLD) r.incrementLeftVelocity();
                else r.decrementLeftVelocity();
                if(outputs[1]>NN_THRESHOLD) r.incrementRightVelocity();
                else r.decrementRightVelocity();
            },1500,8, TimeUnit.MILLISECONDS)).collect(Collectors.toUnmodifiableList());

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
                var nn=new RecurrentNeuralNetwork(1, 50, new int[]{12,4,2});
                controllers[i]=nn;
                nn.setInputByReference(robots[i].proximitySensors);   // hook up proximity sensors to input of nn
            }
        }

        public void breed(){
            System.err.println("Breeding a better generation!");
            var winners=Evaluation.getBestBots(robots,NUMBER_OF_REPRODUCERS)
                    .stream()
                    .mapToInt(Robo::getId)
                    .mapToObj(id->controllers[id])
                    .collect(Collectors.toUnmodifiableList());
            // mutate/crossover logic goes here
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
        add(new RoboPanel(bots));

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
                    case 'e':
                        controllerHandle.forEach(h->h.cancel(true));
                    case 'r': Arrays.stream(bots).forEach(b->b.setPosition(bounds.getCenterX(),bounds.getCenterY()));  break;
                    case 't': robo.incrementBothVelocity(); break;
                    case 'g': robo.decrementBothVelocity();  break;
                    case 'b': robo.setPosition(bounds.getCenterX(),bounds.getCenterY()); break;
                    default: controllerHandle.forEach(h->h.cancel(true));
                        System.err.println("Stopping all controllers!");
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
