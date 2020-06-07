import ai.IRobotController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;

public class DebugPanel extends JPanel {
    private final IRobotController[] networks;
    public DebugPanel(IRobotController[] networks) {
        this.networks = networks;
        setPreferredSize(new Dimension(networks.length*15+7,networks[0].getOutput().length*15+15));
        setBackground(new Color(0,0,0,0.2f));
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
