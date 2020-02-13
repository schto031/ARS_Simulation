import java.awt.*;
import java.awt.geom.Point2D;

public class Robo implements Runnable, Drawable {
    protected double x=400,y=300, vl, vr, orientation;
    protected final double width;
    private final Runnable postUpdateHook;
    private final double delta =0.1;
    private final double[] proximitySensors=new double[12];

    public Robo(double width, Runnable postUpdateHook) {
        this.width=width;
        this.postUpdateHook = postUpdateHook;
    }

    @Override
    public void draw(Graphics2D graphics){
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawOval((int)x,(int)y,(int)width,(int)width);
        var halfWidth=width/2;
        var midX=x+halfWidth;
        var midY=y+halfWidth;
        graphics.drawLine((int)midX,(int)midY,(int)(midX+halfWidth*Math.cos(-orientation)),(int)(midY-halfWidth*Math.sin(-orientation)));
        drawSensors(graphics, midX, midY);
    }

    @Override
    public void run() {
        var v=(vl + vr)/2;
        if(vl == vr){
            x+=v*Math.cos(-orientation)* delta;
            y-=v*Math.sin(-orientation)* delta;
        } else if(vl ==-vr){
            var omega=(vr - vl)/width;
            orientation+=omega* delta;
            x+=v*Math.cos(-orientation)* delta;
            y-=v*Math.sin(-orientation)* delta;
        } else {
            var R=(width/2)*(vl + vr)/(vr - vl);
            var omega=(vr - vl)/width;
            var ICCX=x-(R*Math.sin(orientation));
            var ICCY=y+(R*Math.cos(orientation));
            x=(Math.cos(omega*delta)*(x-ICCX)+ICCX)-Math.sin(omega* delta)*(y-ICCY);
            y=(Math.sin(omega*delta)*(x-ICCX)+ICCY)+Math.cos(omega* delta)*(y-ICCY);
            orientation+=omega*delta;
        }
        postUpdateHook.run();
    }

    private void drawSensors(Graphics2D graphics, double midX, double midY){
        Color[] colors={new Color(1,0,0,0.3f), new Color(0,0,0,0f)};
        var beamStrength=100;
        float[] dist={0f,1f};
        var radialGradientPaint=new RadialGradientPaint(new Point2D.Float((float) x,(float) y), beamStrength, dist, colors);
        graphics.setPaint(radialGradientPaint);
        for(var i=0d;i<Math.PI*2;i+=(Math.PI*2/proximitySensors.length)){
            graphics.drawLine((int)midX, (int)midY, (int)(midX+beamStrength*Math.cos(-orientation+i)),(int)(midY-beamStrength*Math.sin(-orientation+i)));
        }
    }

    @Override
    public String toString() {
        return "Robo{" +
                "x=" + x +
                ", y=" + y +
                ", vX=" + vl +
                ", vY=" + vr +
                ", orientation=" + orientation +
                ", width=" + width +
                '}';
    }
}
