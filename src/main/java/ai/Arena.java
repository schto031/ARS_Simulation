package ai;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class Arena {
    private static final double PADDING=20;

    public static List<Line2D> getBoxedArena(Rectangle2D bounds){
        var upperBorder = new Line2D.Double(20,20,780,20);
        var lowerBorder = new Line2D.Double(20,580,780,580);
        var leftBorder = new Line2D.Double(20,20,20,580);
        var rightBorder = new Line2D.Double(780,20,780,580);

        return List.of(upperBorder,lowerBorder,leftBorder,rightBorder);
    }
}
