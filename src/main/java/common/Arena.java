package common;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Arena {
    private List<Line2D> obstacles;
    private Coordinate.Double initialLocation;
    private List<Coordinate.Double> beacons=new ArrayList<>();

    public Arena(List<Line2D> obstacles, Coordinate.Double initialLocation) {
        this.obstacles = obstacles;
        this.initialLocation = initialLocation;
    }

    public Arena(List<Line2D> obstacles, Coordinate.Double initialLocation, List<Coordinate.Double> beacons) {
        this.obstacles = obstacles;
        this.initialLocation = initialLocation;
        this.beacons = beacons;
    }

    public List<Line2D> getObstacles() { return obstacles; }
    public Coordinate.Double getInitialLocation() { return initialLocation; }
    public List<Coordinate.Double> getBeacons() { return beacons; }

    public static Arena getKalmann(){
        var upperBorder = (Line2D) new Line2D.Double(20,20,780,20);
        var lowerBorder = (Line2D) new Line2D.Double(20,580,780,580);
        var leftBorder = (Line2D) new Line2D.Double(20,20,20,580);
        var rightBorder = (Line2D) new Line2D.Double(780,20,780,580);
        var w1 = (Line2D) new Line2D.Double(20,100, 600, 100);
        var w2 = (Line2D) new Line2D.Double(780,200, 200, 200);
        var w3 = (Line2D) new Line2D.Double(200,200, 200, 500);
        var w4 = (Line2D) new Line2D.Double(500,300, 500, 580);

        var initialLocation=new Coordinate.Double(20,50);

        var obstacles=List.of(upperBorder, lowerBorder, leftBorder, rightBorder, w1, w2, w3, w4);
        var beacons= obstacles
                .stream()
                .map(o->List.of(o.getP1(), o.getP2()))
                .flatMap(List::stream)
                .distinct()
                .map(p->new Coordinate.Double(p.getX(), p.getY())).collect(Collectors.toUnmodifiableList());
        return new Arena(obstacles, initialLocation, beacons);
    }

    public static Arena getBoxed(){
        var upperBorder = new Line2D.Double(20,20,780,20);
        var lowerBorder = new Line2D.Double(20,380,780,380);
        var leftBorder = new Line2D.Double(20,20,20,380);
        var rightBorder = new Line2D.Double(780,20,780,380);
        var initialLocation=new Coordinate.Double(400,300);
        return new Arena(List.of(upperBorder, lowerBorder, leftBorder, rightBorder), initialLocation);
    }
}