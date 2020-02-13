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
        graphics.drawLine((int)(x+width/2),(int)(y+width/2),(int)(x+width*Math.cos(orientation)),(int)(y+width*Math.sin(orientation)));
    }

    @Override
    public String toString() {
        return "Robo{" +
                "x=" + x +
                ", y=" + y +
                ", vX=" + vX +
                ", vY=" + vY +
                '}';
    }

    @Override
    public void run() {
        x+=vX*FRAME_SKIP;
        y+=vY*FRAME_SKIP;
        postUpdateHook.run();
//        System.out.println(this);
    }
}
