import ai.NeuralNetwork;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;

public class DebugPanel extends JPanel {
    private final NeuralNetwork[] networks;
    public DebugPanel(NeuralNetwork[] networks) {
        this.networks = networks;
        setPreferredSize(new Dimension(networks.length*15+15,30));
        setBackground(new Color(0,0,0,0.2f));
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
        var i=5;
        for(var n:networks){
            if(n.getOutput()[0]>10)
                g.setColor(Color.GREEN);
            else
                g.setColor(Color.RED);
            ((Graphics2D)g).fill(new Ellipse2D.Double(i,10,10,10));
            i+=15;
        }
    }
}
