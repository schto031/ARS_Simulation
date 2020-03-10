import ai.IRobotController;
import ai.RobotController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class DebugPanel extends JPanel {
    private final IRobotController[] networks;
    private final List<Robo> bestRobots;
    private final AtomicInteger generation;
    private final JLabel generationLabel;
    private static final Color BACKGROUND_COLOR=new Color(0,0,0,0.2f);
    private static final Color TRANSPARENT=new Color(0,0,0,0);

    public DebugPanel(RobotController[] networks, List<Robo> bestRobots, AtomicInteger generation) {
        this.networks = networks;
        this.bestRobots = bestRobots;
        this.generation=generation;
        setBackground(BACKGROUND_COLOR);
        generationLabel=new JLabel();
        add(generationLabel);
        add(new BestGenerationPanel());
        add(new ControllerPanel());
        setToolTipText("Debugs the output layer of each robot. Drag to reposition!");
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                setLocation(mouseEvent.getLocationOnScreen());
            }

            @Override
            public void mouseMoved(MouseEvent mouseEvent) { }
        });
    }
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        generationLabel.setText(String.format("Generation %d", generation.get()));
    }

    private class ControllerPanel extends JPanel{
        ControllerPanel(){
            setPreferredSize(new Dimension(networks.length*15+7,networks[0].getOutput().length*15+15));
            setBackground(TRANSPARENT);
        }
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            var i=5;
            for(var n:networks){
                var j=10;
                for(var o:n.getOutput()){
                    if(o>10)
                        g.setColor(Color.GREEN);
                    else
                        g.setColor(Color.RED);
                    ((Graphics2D)g).fill(new Ellipse2D.Double(i,j,10,10));
                    j+=15;
                }
                i+=15;
            }
        }
    }

    private class BestGenerationPanel extends JPanel{
        private final JLabel label;
        BestGenerationPanel(){
            setBackground(TRANSPARENT);
            label=new JLabel();
            add(label);
        }
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            var sb=new StringBuffer();
            sb.append("<html>");
            bestRobots.forEach(b->sb.append(b.getId()).append(" ").append(Evaluation.evaluationFunction(b)).append("<br>"));
            sb.append("</html>");
            label.setText(sb.toString());
        }
    }
}
