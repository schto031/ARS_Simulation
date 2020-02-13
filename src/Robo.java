import java.awt.*;

public class Robo implements Runnable {
    protected double x,y,vX,vY,orientation;
    protected final double width;
    private final Runnable postUpdateHook;
    private final double FRAME_SKIP=0.1;

    public Robo(double width, Runnable postUpdateHook) {
        this.width=width;
        this.postUpdateHook = postUpdateHook;
    }

    public void draw(Graphics2D graphics){
        graphics.drawOval((int)x,(int)y,(int)width,(int)width);
        var halfWidth=width/2;
        var midX=x+halfWidth;
        var midY=y+halfWidth;
        var radians=Math.toRadians(orientation);
        graphics.drawLine((int)midX,(int)midY,(int)(midX+halfWidth*Math.sin(radians)),(int)(midY+halfWidth*Math.cos(radians)));
    }

    @Override
    public String toString() {
        return "Robo{" +
                "x=" + x +
                ", y=" + y +
                ", vX=" + vX +
                ", vY=" + vY +
                ", orientation=" + orientation +
                ", width=" + width +
                '}';
    }

    @Override
    public void run() {
        var v=(vX+vY)/2;
        var radians=Math.toRadians(orientation);
        x+=v*Math.sin(radians)*FRAME_SKIP;
        y+=v*Math.cos(radians)*FRAME_SKIP;

        postUpdateHook.run();
//        System.out.println(this);
    }
}
