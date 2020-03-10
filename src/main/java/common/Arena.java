package common;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class Arena {
    private static final double PADDING=20;
    private final List<Line2D> obstacles;
    private final Coordinate.Double initialLocation;

    public Arena(List<Line2D> obstacles, Coordinate.Double initialLocation) {
        this.obstacles = obstacles;
        this.initialLocation = initialLocation;
    }

    public static Arena getBoxedArena(Rectangle2D bounds){
        var upperBorder = new Line2D.Double(20,20,780,20);
        var lowerBorder = new Line2D.Double(20,580,780,580);
        var leftBorder = new Line2D.Double(20,20,20,580);
        var rightBorder = new Line2D.Double(780,20,780,580);
        return new Arena(List.of(upperBorder,lowerBorder,leftBorder,rightBorder), new Coordinate.Double(400, 300));
    }

    public static Arena getDoubleBoxedArena(Rectangle2D bounds){
        var ba=getBoxedArena(bounds);
        var obstacles=new ArrayList<>(ba.getObstacles());
        obstacles.add(new Line2D.Double(177,245,177,512));
        obstacles.add(new Line2D.Double(177,245,641,245));
        obstacles.add(new Line2D.Double(641,245,641,512));
        obstacles.add(new Line2D.Double(177,512,641,512));
        return new Arena(obstacles, new Coordinate.Double(75, 157));
    }

    public List<Line2D> getObstacles() { return obstacles; }
    public Coordinate.Double getInitialLocation() { return initialLocation; }
}
