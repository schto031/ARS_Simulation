import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Double;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Runner extends JFrame {
    private static class RoboPanel extends JPanel{
    	
        private volatile Robo robo;
        private Shape rectangle2D,line2D;
        private Line2D upperBorder,lowerBorder,leftBorder,rightBorder;
        
      //Written by Swapneel + Tom
        public RoboPanel(Robo robo) {
            this.robo=robo;
            var executor=new ScheduledThreadPoolExecutor(1);
            executor.scheduleAtFixedRate(robo,0,8, TimeUnit.MILLISECONDS);  // Roughly 120 FPS if your machine can support
            executor.scheduleWithFixedDelay(()-> System.out.println(Arrays.toString(robo.proximitySensors)+" "+robo),0,1, TimeUnit.SECONDS);
            var rand=new Random();
            rectangle2D=new Line2D.Double(rand.nextInt(200), rand.nextInt(800), rand.nextInt(100), rand.nextInt(150));
            line2D=new Line2D.Double(rand.nextInt(400), rand.nextInt(800), rand.nextInt(100), rand.nextInt(150));
            upperBorder = new Line2D.Double(20,20,780,20);
            lowerBorder = new Line2D.Double(20,380,780,380);
            leftBorder = new Line2D.Double(20,20,20,380);
            rightBorder = new Line2D.Double(780,20,780,380);
			robo.setObstacles(List.of((Line2D) line2D,(Line2D) rectangle2D, upperBorder,lowerBorder,leftBorder,rightBorder));
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
            ((Graphics2D) g).draw(rectangle2D);
            ((Graphics2D) g).draw(line2D);
            ((Graphics2D) g).draw(upperBorder);
            ((Graphics2D) g).draw(lowerBorder);
            ((Graphics2D) g).draw(leftBorder);
            ((Graphics2D) g).draw(rightBorder);
        }
    }
  //Written by Swapneel
    private Runner() {
        setSize(new Dimension(800,600));
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
                    case 'w': robo.vr++; break;
                    case 's': robo.vr--; break;
                    case 'o': robo.vl++; break;
                    case 'l': robo.vl--; break;
                    case 'd': robo.orientation++; break;
                    case 'f': robo.orientation--; break;
                    case 'x': robo.vl =0; robo.vr =0; break;
                    case 'r':
                        var bounds=getBounds();
                        robo.pos.x=(double) bounds.width/2;robo.pos.y=(double) bounds.height/2; break;
                    case 't': robo.vl++; robo.vr++; break;
                    case 'g': robo.vl--; robo.vr--;  break;
                    default:
                }
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) { }
        });
        getRootPane().registerKeyboardAction(e->{ System.exit(0); }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "true");
        var runner=new Runner();
        SwingUtilities.invokeLater(()->runner.setVisible(true));
    }
}
