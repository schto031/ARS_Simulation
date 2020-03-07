import ai.Arena;
import ai.NeuralNetwork;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Runner extends JFrame {
    private static final byte NUMBER_OF_ROBOTS=16;

    private static class RoboPanel extends JPanel{
        private Robo[] robots;
        private NeuralNetwork[] controllers;
        private List<Line2D> obstacles;
        
        //Written by Swapneel + Tom
        public RoboPanel(Robo... robo) {
            this.robots=robo;
            setPreferredSize(new Dimension(800,600));
//            setLayout(null);

            obstacles= Arena.getBoxedArena(getBounds());
            // Set all obstacles
            Arrays.stream(robots).forEach(r-> r.setObstacles(obstacles));
            // Initalize robots with random velocities
            Arrays.stream(robots).forEach(r-> r.setVelocity((_vl)->Math.random()-0.5, (_vr)->Math.random()-0.5));
            // Initialize a scheduler
            var executor=new ScheduledThreadPoolExecutor(8);
            // Robots position calculation thread
            Arrays.stream(robo).forEach(r->executor.scheduleAtFixedRate(r,500,8, TimeUnit.MILLISECONDS));
            // Display thread
            executor.scheduleAtFixedRate(()->SwingUtilities.invokeLater(this::repaint),0,8, TimeUnit.MILLISECONDS);
            // Printing thread
            executor.scheduleWithFixedDelay(()-> System.out.println(Arrays.toString(robo[0].proximitySensors)+" "+ Arrays.toString(robo)),1,1, TimeUnit.SECONDS);
            // Initialize neural network for every bot
            controllers=new NeuralNetwork[robots.length];
            initializeNeuralNetwork();
            // Controller debug thread
            executor.scheduleWithFixedDelay(()-> System.err.println(Arrays.toString(controllers[0].getInput())),1,1, TimeUnit.SECONDS);
            // Set a threshold above which NN is triggered
            var threshold=10;
            // NN control thread
            Arrays.stream(robo).forEach(r->executor.scheduleAtFixedRate(()->{
                var controller=controllers[r.getId()];
                controller.forwardPropagate();
                var outputs=controller.getOutput();
                if(outputs[0]>threshold) r.incrementLeftVelocity();
                else r.decrementLeftVelocity();
                if(outputs[1]>threshold) r.incrementRightVelocity();
                else r.decrementRightVelocity();
            },0,8, TimeUnit.MILLISECONDS));

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
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Arrays.stream(robots).forEach(r->r.draw((Graphics2D) g));
            obstacles.forEach(((Graphics2D) g)::draw);
        }

        private void initializeNeuralNetwork(){
            for(var i=0;i<controllers.length;i++){
                var nn=new NeuralNetwork(12,4,2);
                controllers[i]=nn;
                nn.setInputByReference(robots[i].proximitySensors);   // hook up proximity sensors to input of nn
            }
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
                    case 'r': Arrays.stream(bots).forEach(b->b.setPosition(bounds.getCenterX(),bounds.getCenterY()));  break;
                    case 't': robo.incrementBothVelocity(); break;
                    case 'g': robo.decrementBothVelocity();  break;
                    case 'b': robo.setPosition(bounds.getCenterX(),bounds.getCenterY()); break;
                    default:
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
