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
            new ScheduledThreadPoolExecutor(1).scheduleAtFixedRate(robo,0,10, TimeUnit.MILLISECONDS);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            robo.draw((Graphics2D) g);
        }
    }

    private Runner() throws InterruptedException {
        setSize(new Dimension(800,600));
        var robo=new Robo(10, ()->SwingUtilities.invokeLater(this::repaint));
        add(new RoboPanel(robo));
        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) { }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                switch (keyEvent.getKeyChar()){
                    case 'w': robo.vX++; break;
                    case 's': robo.vX--; break;
                    case 'o': robo.vY++; break;
                    case 'l': robo.vY--; break;
                    case 'x': robo.vX=0; robo.vY=0; break;
                    case 'r': robo.x=0; robo.y=0; break;
                    default:
                }
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) { }
        });
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) throws InterruptedException {
        var runner=new Runner();
        SwingUtilities.invokeLater(()->runner.setVisible(true));
    }
}
