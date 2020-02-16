import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class Robo implements Runnable, Drawable {
    protected Coordinate.Double pos=new Coordinate.Double(400,300);
    private double halfWidth;
    private Coordinate.Double center=new Coordinate.Double(pos.x+halfWidth, pos.y+halfWidth);
    protected double vl, vr, orientation;
    protected final double width;
    private final Runnable postUpdateHook;
    private final double delta =0.1;
    private final double[] proximitySensors=new double[12];
    private Shape ellipse=new Ellipse2D.Double();
    private List<Line2D> obstacles=new ArrayList<>();
    private ConcurrentHashMap<Line2D, Boolean> shortest=new ConcurrentHashMap<>(obstacles.size());

    public Robo(double width, Runnable postUpdateHook) {
        this.width=width;
        this.halfWidth=width/2;
        this.postUpdateHook = postUpdateHook;
    }

    @Override
    public void draw(Graphics2D g){
        final var graphics = (Graphics2D)g.create();
        try{
            ellipse=new Ellipse2D.Double(pos.x,pos.y,width,width);
            graphics.draw(ellipse);
            halfWidth=width/2;
            var midX=pos.x+halfWidth;
            var midY=pos.y+halfWidth;
            center=new Coordinate.Double(midX, midY);
            graphics.drawLine((int)midX,(int)midY,(int)(midX+halfWidth*Math.cos(-orientation)),(int)(midY-halfWidth*Math.sin(-orientation)));
            drawSensors(graphics, midX, midY);
            if (null!=shortest) { drawLine(graphics, shortest.keySet()); }
        } finally {
            graphics.dispose();
        }
    }

    public void setObstacles(List<Line2D> obstacles) { this.obstacles = obstacles; }

    private boolean collisionFunction(Coordinate.Double p){
        return obstacles.parallelStream().anyMatch(o->{
            var cp=center.getClosestPointOnSegment(o);
            var m=cp.distance(center);
            var intersects= m<=(width/2);
            shortest.put(new Line2D.Double(center, cp), intersects);
            return intersects;
        });
    }

    private Coordinate.Double normalize(Line2D line2D){
        var magnitude=line2D.getP1().distance(line2D.getP2());
        var x=(line2D.getX2()-line2D.getX1())/magnitude;
        var y=(line2D.getY2()-line2D.getY1())/magnitude;
        return new Coordinate.Double(x,y);
    }

    private Coordinate.Double collisionHandler(Coordinate.Double point2D, Stream<Map.Entry<Line2D, Boolean>> filteredCollidingLines){
        var magnitude=filteredCollidingLines.map(c-> normalize(c.getKey()))
                .reduce((a,d)->{
                    a.add(d);
                    return a;
                }).orElse(new Coordinate.Double(0,0));
        magnitude.minus();
        magnitude.multiply(delta);
        point2D.add(magnitude);
        return point2D;
    }

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
        postUpdateHook.run();
    }

    private void drawSensors(Graphics2D graphics, double midX, double midY){
        Color[] colors={new Color(1,0,0,0.3f), new Color(0,0,0,0f)};
        var beamStrength=width*3;
        float[] dist={0f,1f};
        var radialGradientPaint=new RadialGradientPaint(new Point2D.Double(midX, midY), (float) beamStrength, dist, colors);
        graphics.setPaint(radialGradientPaint);
        for(var i=0d;i<Math.PI*2;i+=(Math.PI*2/proximitySensors.length)){
            graphics.drawLine((int)midX, (int)midY, (int)(midX+beamStrength*Math.cos(-orientation+i)),(int)(midY-beamStrength*Math.sin(-orientation+i)));
        }
    }

    private void drawLine(Graphics2D graphics, Collection<Line2D> line2D){
        graphics.setColor(Color.YELLOW);
        for(var l:line2D){ graphics.draw(l); }
        shortest.clear();
    }

    @Override
    public String toString() {
        return "Robo{" +
                "x=" + pos.x +
                ", y=" + pos.y +
                ", vX=" + vl +
                ", vY=" + vr +
                ", orientation=" + orientation +
                ", width=" + width +
                '}';
    }
}
