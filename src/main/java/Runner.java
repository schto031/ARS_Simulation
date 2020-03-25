import common.Arena;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.Arrays;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Runner extends JFrame {
    private static class RoboPanel extends JPanel{

        private volatile Robo robo;
        private Arena arena= Arena.getKalmann();
        private Path2D robotPathTrace =new Path2D.Double();

        //Written by Swapneel + Tom
        public RoboPanel(Robo robo) {
            this.robo=robo;
            robo
                    .setBeacons(arena.getBeacons())
                    .setPosition(arena.getInitialLocation());
            var executor=new ScheduledThreadPoolExecutor(1);
            executor.scheduleAtFixedRate(robo,0,8, TimeUnit.MILLISECONDS);  // Roughly 120 FPS if your machine can support
            executor.scheduleWithFixedDelay(()-> System.out.println(Arrays.toString(robo.proximitySensors)+" "+robo),0,1, TimeUnit.SECONDS);
            robo.setObstacles(arena.getObstacles());
            robotPathTrace.moveTo(robo.getCenter().x, robo.getCenter().y);
            addMouseMotionListener(new MouseMotionListener() {
                @Override
                public void mouseDragged(MouseEvent mouseEvent) { }

                @Override
                public void mouseMoved(MouseEvent mouseEvent) {
                    System.out.println(mouseEvent.getLocationOnScreen());
                }
            });
        }

        @Override
        //Written by Swapneel + Tom
        public void paint(Graphics g) {
            super.paint(g);
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            robo.draw((Graphics2D) g);

            // Draw obstacles
            for(var a:arena.getObstacles())
                ((Graphics2D) g).draw(a);

            // Draw balls (beacons)
            var rad=10D;
            for(var b:arena.getBeacons()){
                ((Graphics2D) g).setPaint(new Color(0f,0.8f, 0.8f, 0.5f));
                var e=new Ellipse2D.Double(b.x-rad/2, b.y-rad/2, rad, rad);
                ((Graphics2D) g).fill(e);
            }

            // Draw trace
            var center=robo.getCenter();
            ((Graphics2D) g).setStroke(new BasicStroke(5));
            ((Graphics2D) g).setPaint(new Color(0.8f,0.2f, 0.8f, 0.5f));
            robotPathTrace.lineTo(center.x, center.y);
            ((Graphics2D) g).draw(robotPathTrace);
        }
    }
    //Written by Swapneel
    private Runner() {
        setSize(new Dimension(800,650));
        setTitle("Low budget robot simulator");
        var menu=new JMenu("Options");
        var menuBar=new JMenuBar();
        var reconfigure=new JMenuItem("Reconfigure");
        reconfigure.addActionListener(a-> JOptionPane.showMessageDialog(this, "This does nothing yet"));
        menu.add(reconfigure);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        var robo=new Robo(36, ()->SwingUtilities.invokeLater(this::repaint));
        add(new RoboPanel(robo));
        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) { }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                switch (keyEvent.getKeyChar()){
                    case 'w': robo.incrementBothVelocity(); break;
                    case 's': robo.decrementBothVelocity(); break;
                    case 'a': robo.incrementLeftVelocity(); robo.decrementRightVelocity(); break;
                    case 'd': robo.decrementLeftVelocity(); robo.incrementRightVelocity(); break;
//                    case 'w': robo.incrementRightVelocity(); break;
//                    case 's': robo.decrementRightVelocity(); break;
//                    case 'o': robo.incrementLeftVelocity(); break;
//                    case 'l': robo.decrementLeftVelocity(); break;
                    case 'x': robo.stop(); break;
                    case 'r':
                        var bounds=getBounds();
                        robo.setPosition((double) bounds.width/2,robo.pos.y=(double) bounds.height/2);
                        break;
//                    case 't': robo.incrementBothVelocity(); break;
//                    case 'g': robo.decrementBothVelocity();  break;
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