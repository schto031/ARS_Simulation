import common.Arena;
import common.DashedStroke;
import org.ejml.simple.SimpleMatrix;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Runner extends JFrame {
    private static class RoboPanel extends JPanel{

        private volatile Robo robo;
        private Arena arena= Arena.getKalmann();
        private Path2D robotPathTrace =new Path2D.Double();
        private Path2D robotPredictTrace =new Path2D.Double();
        private Path2D robotCorrectTrace =new Path2D.Double();
        private final List<Shape> predictedCovs=new ArrayList<>();
        private final List<Shape> correctedCovs=new ArrayList<>();
        private KalmanFilter kalmanFilter;
        private final int sampleAtNumberOfTicks=10;

        //Written by Swapneel + Tom
        public RoboPanel(Robo robo) {
            this.robo=robo;
            robo
                    .setBeacons(arena.getBeacons())
                    .setPosition(arena.getInitialLocation());
            kalmanFilter=new KalmanFilter(robo);
            var executor=new ScheduledThreadPoolExecutor(1);
            executor.scheduleAtFixedRate(robo,0,16, TimeUnit.MILLISECONDS);  // Roughly 120 FPS if your machine can support
//            executor.scheduleWithFixedDelay(()-> System.out.println(Arrays.toString(robo.proximitySensors)+" "+robo),0,1, TimeUnit.SECONDS);
            executor.scheduleWithFixedDelay(()->kalmanFilter.getValues(),0,1, TimeUnit.SECONDS);
            var tick=new AtomicLong(1);
            executor.scheduleAtFixedRate(()->{
                try{

                    var orientation=robo.orientation;
                    // Individual beacons and their corresponding functions in radial coordinate system
                    var fZ=new SimpleMatrix(3,robo.getBeaconsInRange().size());
                    var i=0;
                    for(var b:robo.getBeaconsInRange()){
                        var center=robo.getCenter();
                        fZ.set(0,i,center.distance(b));
                        fZ.set(1,i,Math.atan2(b.y-center.y, b.x-center.x)-orientation);
                        fZ.set(2,i,i++);
                    }
                    // Average of all beacons in radial coordinate system
                    var tZ=new SimpleMatrix(3,1);
                    for(i=0;i<fZ.numRows();i++){
                        var t=fZ.extractVector(true, i);
                        var mean=t.elementSum()/t.getMatrix().data.length;
                        tZ.set(i,0, mean);
                    }
                    // Convert it to cartesian
                    var Z=new SimpleMatrix(3,1);
                    Z.set(0,0, tZ.get(0,0)*Math.cos(orientation));
                    Z.set(1,0, tZ.get(0,0)*Math.sin(orientation));
                    Z.set(2,0, tZ.get(1,0));

                    // Add some noise
                    var r=new Random();
                    var scalingFactor=10;
                    var noise=new SimpleMatrix(3, 1, true,
                            r.nextGaussian()*scalingFactor,
                            r.nextGaussian()*scalingFactor,
                            r.nextGaussian()*scalingFactor);
                    Z=Z.plus(noise);

                    var covariance= SimpleMatrix.identity(3);

                    var b=kalmanFilter.predict(robo.getPose(), covariance, robo.getMotionModel(), Z);
                    var x=b.mu.get(0,0);
                    var y=b.mu.get(1,0);
                    robotPredictTrace.lineTo(x,y);
                    if(tick.get()%sampleAtNumberOfTicks==0) predictedCovs.add(
                            getEllipseFromCenter(x,y,
                                    Math.pow(b.sigma.get(0,0),2),
                                    Math.pow(b.sigma.get(1,1),2),
                                    b.sigma.get(2,2)));
                    x=b.correctedMu.get(0,0);
                    y=b.correctedMu.get(1,0);
                    robotCorrectTrace.lineTo(x, y);
                    if(tick.getAndIncrement()%sampleAtNumberOfTicks==0) correctedCovs.add(
                            getEllipseFromCenter(x,y,
                                    Math.pow(b.correctedSigma.get(0,0),2),
                                    Math.pow(b.correctedSigma.get(1,1),2),
                                    b.correctedSigma.get(2,2)));
                } catch (Exception e){
                    e.printStackTrace();
                }
            },0,64, TimeUnit.MILLISECONDS);

            robo.setObstacles(arena.getObstacles());

            // Initial locations of traces
            robotPathTrace.moveTo(robo.getCenter().x, robo.getCenter().y);
            robotCorrectTrace.moveTo(robo.getCenter().x, robo.getCenter().y);
            robotPredictTrace.moveTo(robo.getCenter().x, robo.getCenter().y);

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

            // Draw actual trace
            var center=robo.getCenter();
            ((Graphics2D) g).setStroke(new BasicStroke(3));
            ((Graphics2D) g).setPaint(new Color(0.2f,0.6f, 0.2f, 0.3f));
            robotPathTrace.lineTo(center.x, center.y);
            ((Graphics2D) g).draw(robotPathTrace);

            // Draw predicted covariance and trace
            ((Graphics2D) g).setPaint(new Color(0.2f,0.2f, 0.8f, 0.6f));
            ((Graphics2D) g).setStroke(new BasicStroke(2));
            predictedCovs.forEach(((Graphics2D) g)::draw);
            ((Graphics2D) g).setPaint(new Color(0.2f,0.2f, 0.8f, 0.3f));
            ((Graphics2D) g).setStroke(new DashedStroke(3));
            ((Graphics2D) g).draw(robotPredictTrace);

            // Draw corrected covariance and trace
            ((Graphics2D) g).setPaint(new Color(0.6f,0.2f, 0.2f, 0.6f));
            ((Graphics2D) g).setStroke(new BasicStroke(2));
            correctedCovs.forEach(((Graphics2D) g)::draw);
            ((Graphics2D) g).setPaint(new Color(0.6f,0.2f, 0.2f, 0.3f));
            ((Graphics2D) g).setStroke(new BasicStroke(3));
            ((Graphics2D) g).draw(robotCorrectTrace);
        }

        private Shape getEllipseFromCenter(double x, double y, double width, double height, double rotation) {
            double newX = x - width / 2.0;
            double newY = y - height / 2.0;
            var ellipse = new Ellipse2D.Double(newX, newY, width, height);
            return AffineTransform.getRotateInstance(rotation, x, y).createTransformedShape(ellipse);
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
                switch (keyEvent.getKeyCode()){
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W: robo.incrementBothVelocity(); break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S: robo.decrementBothVelocity(); break;
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A: robo.incrementLeftVelocity(); robo.decrementRightVelocity(); break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D: robo.decrementLeftVelocity(); robo.incrementRightVelocity(); break;
//                    case 'w': robo.incrementRightVelocity(); break;
//                    case 's': robo.decrementRightVelocity(); break;
//                    case 'o': robo.incrementLeftVelocity(); break;
//                    case 'l': robo.decrementLeftVelocity(); break;
                    case KeyEvent.VK_X: robo.stop(); break;
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