
import common.Coordinate;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Robo implements Runnable, Drawable, IRobotMovement {
	//initialize first position of the robot
    protected Coordinate.Double pos=new Coordinate.Double(400,300);
    private double halfWidth;
    private Coordinate.Double center;
    private double vl, vr, orientation;
    protected final double width;
    private final Runnable postUpdateHook;
    private final double delta =0.05;
    final double[] proximitySensors=new double[12];
    // This will be almost a clone of proximitySensors, but can be resized and manipulated for purposes other than sensors
    double[] inputLayerOfNN=new double[12];
    private Shape robotBody =new Ellipse2D.Double();
    private List<Line2D> obstacles=new ArrayList<>();
    private ConcurrentHashMap<Line2D, Boolean> shortest=new ConcurrentHashMap<>(obstacles.size());
    private Point2D[] allDust;
    private Set<Point2D> coveredDust;
    private final int id;
    private final double SENSOR_MAX=100;
    private final double VELOCITY_MAX=20;
    public AtomicInteger collisions=new AtomicInteger();

    public Robo(double width, Runnable postUpdateHook, int id) {
        this.width=width;
        this.halfWidth=width/2;
        this.postUpdateHook = postUpdateHook;
        this.id=id;
        this.center=new Coordinate.Double(pos.x+halfWidth, pos.y+halfWidth);
        coveredDust=new HashSet<>();
    }

    public Robo(double width, Runnable postUpdateHook) { this(width,postUpdateHook,new Random().nextInt()); }

  //Written by Swapneel
    @Override
    public void draw(Graphics2D g){
        final var graphics = (Graphics2D)g.create();
        try{
            robotBody =new Ellipse2D.Double(pos.x,pos.y,width,width);
            graphics.draw(robotBody);
            halfWidth=width/2;
            var midX=pos.x+halfWidth;
            var midY=pos.y+halfWidth;
            //center of the robot circle
            center=new Coordinate.Double(midX, midY);
            graphics.drawLine((int)midX,(int)midY,(int)(midX+halfWidth*Math.cos(-orientation)),(int)(midY-halfWidth*Math.sin(-orientation)));
            drawSensors(graphics, midX, midY);
            collectDust();
            if (null!=shortest) { drawLine(graphics, shortest.keySet()); }
        } finally {
            graphics.dispose();
        }
    }

  //Written by Swapneel
    public void setObstacles(List<Line2D> obstacles) {
        this.obstacles = obstacles;
        shortest=new ConcurrentHashMap<>(obstacles.size());
    }

  //Written by Swapneel
    private boolean collisionFunction(Coordinate.Double p){
        return obstacles.parallelStream().anyMatch(o->{
            var cp=center.getClosestPointOnSegment(o);
            var m=cp.distance(center);
            var intersects= m<=(width/2);
            shortest.put(new Line2D.Double(center, cp), intersects);
            return intersects;
        });
    }

    //Written by Swapneel
    private Coordinate.Double normalize(Line2D line2D){
        var magnitude=line2D.getP1().distance(line2D.getP2());
        var x=(line2D.getX2()-line2D.getX1())/magnitude;
        var y=(line2D.getY2()-line2D.getY1())/magnitude;
        return new Coordinate.Double(x,y);
    }

    //Written by Swapneel
    private Coordinate.Double collisionHandler(Coordinate.Double point2D, Stream<Map.Entry<Line2D, Boolean>> filteredCollidingLines){
        var magnitude=filteredCollidingLines.map(c-> normalize(c.getKey()))
                .reduce((a,d)->{
                    a.add(d);
                    return a;
                }).orElse(new Coordinate.Double(0,0));
        magnitude.minus();
        collisions.incrementAndGet();
        point2D.add(magnitude);
        return point2D;
    }

    //Written by Swapneel
    @Override
    public void run() {
        var v=(vl + vr)/2;
        var collidingLines=shortest.entrySet().parallelStream().filter(Map.Entry::getValue);
        if(vl == vr){
            pos.testAndUpdate(this::collisionFunction,
                    x->x+v*Math.cos(-orientation)* delta,
                    y->y-v*Math.sin(-orientation)* delta,
                    point2D -> collisionHandler(point2D, collidingLines));
        } else if(vl ==-vr){
            var omega=(vr - vl)/width;
            orientation+=omega* delta;
            pos.testAndUpdate(this::collisionFunction,
                    x->x+v*Math.cos(-orientation)* delta,
                    y->y-v*Math.sin(-orientation)* delta,
                    point2D -> collisionHandler(point2D, collidingLines));
        } else {
            var R=(width/2)*(vl + vr)/(vr - vl);
            var omega=(vr - vl)/width;
            var ICCX=pos.x-(R*Math.sin(orientation));
            var ICCY=pos.y+(R*Math.cos(orientation));
            pos.testAndUpdate(this::collisionFunction,
                    x->(Math.cos(omega*delta)*(pos.x-ICCX)+ICCX)-Math.sin(omega* delta)*(pos.y-ICCY),
                    y->(Math.sin(omega*delta)*(pos.x-ICCX)+ICCY)+Math.cos(omega* delta)*(pos.y-ICCY),
                    point2D -> collisionHandler(point2D, collidingLines));
            orientation+=omega*delta;
        }
        if(null!=postUpdateHook) postUpdateHook.run();
    }

    //written by Swapneel + Tom
    private void drawSensors(Graphics2D graphics, double midX, double midY){
    	//grey and white color
        Color[] colors={new Color(1,0,0,0.3f), new Color(0,0,0,0f)};
        //length of sensor beams
        var beamStrength=width*4;
        float[] dist={0f,1f};
        //color fades for sensor beams
        var radialGradientPaint=new RadialGradientPaint(new Point2D.Double(midX, midY), (float) beamStrength, dist, colors);
        graphics.setPaint(radialGradientPaint);
        //determine the position of the 12 sensor lines
        var j=0;
        for(var i=0d;i<Math.PI*2;i+=(Math.PI*2/proximitySensors.length)){
            var line=new Line2D.Double(center, new Point2D.Double(midX+beamStrength*Math.cos(-orientation+i), midY-beamStrength*Math.sin(-orientation+i)));
            senseDistance(line,j++, beamStrength);
            //line length gets adjusted regarding the possibility of hitting obstacle
            line = new Line2D.Double(center, new Point2D.Double(midX+proximitySensors[j-1]*Math.cos(-orientation+i), midY-proximitySensors[j-1]*Math.sin(-orientation+i)));
            graphics.draw(line);
            Point2D distanceStringPosition = new Point2D.Double(midX+proximitySensors[j-1]*Math.cos(-orientation+i), midY-proximitySensors[j-1]*Math.sin(-orientation+i));
            graphics.setPaint(new Color(1,0,0,0.3f));
            //
            graphics.drawString(Integer.toString((int)proximitySensors[j-1]) , (int) distanceStringPosition.getX(), (int)distanceStringPosition.getY() );
            graphics.setPaint(radialGradientPaint);
        }
    }

    public void collectDust(){
        var collectedDust=Arrays.stream(allDust).filter(d->robotBody.contains(d)).collect(Collectors.toCollection(ArrayList::new));
        this.coveredDust.addAll(collectedDust);
    }

    private void senseDistance(Line2D sensor, int index, double beamStrength){
    	//distances between obstacle and sensor beam origin
        var distance=obstacles.parallelStream()
                .filter(p->p.intersectsLine(sensor))
                .mapToDouble(p->findIntersectionPoint(p,sensor).distance(sensor.getP1())).min();
        double d=0;
        double funct=SENSOR_MAX;
//        if(distance.isPresent()) { d=distance.getAsDouble()*SENSOR_MAX/beamStrength; }
//        else d=SENSOR_MAX;
        if(distance.isPresent()) {
            d=distance.getAsDouble()*SENSOR_MAX/beamStrength;
            funct=Math.exp(width-d);
        }
        proximitySensors[index]=d;
        inputLayerOfNN[index]=funct;
    }

    //finds intersection point no matter what the orientation of the lines
    //written by Tom Scholer
    private static Point2D findIntersectionPoint(Line2D l1, Line2D l2) {
        double a1 = l1.getY2() - l1.getY1();
        double b1 = l1.getX1() - l1.getX2();
        double c1 = a1 * l1.getX1() + b1 * l1.getY1();
 
        double a2 = l2.getY2() - l2.getY1();
        double b2 = l2.getX1()- l2.getX2();
        double c2 = a2 * l2.getX1() + b2 * l2.getY1();
 
        double delta = a1 * b2 - a2 * b1;
        return new Point2D.Double((b2 * c1 - b1 * c2) / delta, (a1 * c2 - a2 * c1) / delta);
    }

    private void drawLine(Graphics2D graphics, Collection<Line2D> line2D){
        graphics.setColor(Color.YELLOW);
        for(var l:line2D){ graphics.draw(l); }
        shortest.clear();
    }

    public int getId() { return id; }

    public Point2D[] getAllDust() { return allDust; }

    public Robo setAllDust(Point2D[] allDust) {
        this.allDust = allDust;
        return this;
    }

    public int getCollectedDust(){ return coveredDust.size(); }

    @Override
    public String toString() {
        return "Robo{" +
                "x=" + pos.x +
                ", y=" + pos.y +
                ", vX=" + vl +
                ", vY=" + vr +
                ", orientation=" + orientation +
                ", width=" + width +
                ", obstacles="+obstacles.size()+
                ", id="+id+
                ", collisions="+collisions+
                ", dust="+getCollectedDust()+
                '}';
    }

    @Override
    public void incrementLeftVelocity() { if(vl<this.VELOCITY_MAX) vl++; }

    @Override
    public void incrementRightVelocity() { if(vr<this.VELOCITY_MAX) vr++; }

    @Override
    public void decrementLeftVelocity() { if(vl>-this.VELOCITY_MAX) vl--; }

    @Override
    public void decrementRightVelocity() { if(vr>-this.VELOCITY_MAX) vr--; }

    @Override
    public void incrementBothVelocity() { incrementLeftVelocity(); incrementRightVelocity(); }

    @Override
    public void decrementBothVelocity() { decrementLeftVelocity(); decrementRightVelocity(); }

    @Override
    public void stop() { vl=0; vr=0; }

    @Override
    public void setPosition(double x, double y) { this.pos.x=x; this.pos.y=y; }

    @Override
    public void setVelocity(Function<Double, Double> left, Function<Double, Double> right) {
        this.vl=left.apply(this.vl);
        this.vr=right.apply(this.vr);
    }

    @Override
    public double[] getSensorValues() { return this.proximitySensors; }

    public double getDifferentialVelocity(){ return vl-vr; }

    public void resetParameters(){
        coveredDust.clear();
        collisions.set(0);
    }
}
