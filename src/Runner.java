import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Runner extends JFrame {
    private static class RoboPanel extends JPanel{
        private volatile Robo robo;
        public RoboPanel(Robo robo) {
            this.robo=robo;
            var executor=new ScheduledThreadPoolExecutor(1);
            executor.scheduleAtFixedRate(robo,0,10, TimeUnit.MILLISECONDS);
            executor.scheduleWithFixedDelay(()-> System.out.println(robo),0,1, TimeUnit.SECONDS);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            robo.draw((Graphics2D) g);
        }
    }

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

        var robo=new Robo(20, ()->SwingUtilities.invokeLater(this::repaint));
        add(new RoboPanel(robo));
        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) { }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                switch (keyEvent.getKeyChar()){
                    case 'w': robo.vl++; break;
                    case 's': robo.vl--; break;
                    case 'o': robo.vr++; break;
                    case 'l': robo.vr--; break;
                    case 'd': robo.orientation++; break;
                    case 'f': robo.orientation--; break;
                    case 'x': robo.vl =0; robo.vr =0; break;
                    case 'r': robo.x=400;robo.y=300; break;
                    case 't': robo.vl++; robo.vr++; break;
                    case 'g': robo.vl--; robo.vr--;  break;
                    default:
                }
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) { }
        });
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "true");
        var runner=new Runner();
        SwingUtilities.invokeLater(()->runner.setVisible(true));
    }
}
