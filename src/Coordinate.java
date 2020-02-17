import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.function.Function;
import java.util.function.Predicate;

public class Coordinate {
	
    public static class Double extends Point2D {
        protected double x,y;
        public Double(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void testAndUpdate(Predicate<Coordinate.Double> predicate,
                                  Function<java.lang.Double, java.lang.Double> updateFunctionX,
                                  Function<java.lang.Double, java.lang.Double> updateFunctionY,
                                  Function<Coordinate.Double, Coordinate.Double> collisionResolution){
            var updatedCoordinate=new Coordinate.Double(updateFunctionX.apply(x), updateFunctionY.apply(y));
            var collision=predicate.test(updatedCoordinate);
            if(collision){ collisionResolution.apply(this); }
            else{
                x=updatedCoordinate.x;
                y=updatedCoordinate.y;
            }
        }

        @Override
        public double getX() { return x; }

        @Override
        public double getY() { return y; }

        @Override
        public void setLocation(double v, double v1) {
            this.x=v;
            this.y=v1;
        }

        /**
         * Dug up from http://www.java2s.com/Code/Java/2D-Graphics-GUI/Returnsclosestpointonsegmenttopoint.htm
         * @param sx1
         * @param sy1
         * @param sx2
         * @param sy2
         * @param px
         * @param py
         * @return closest point
         */
        public static Double getClosestPointOnSegment(double sx1, double sy1, double sx2, double sy2, double px, double py)
        {
            double xDelta = sx2 - sx1;
            double yDelta = sy2 - sy1;

            if ((xDelta == 0) && (yDelta == 0))
            {
                throw new IllegalArgumentException("Segment start equals segment end");
            }

            double u = ((px - sx1) * xDelta + (py - sy1) * yDelta) / (xDelta * xDelta + yDelta * yDelta);

            final Double closestPoint;
            if (u < 0)
            {
                closestPoint = new Double(sx1, sy1);
            }
            else if (u > 1)
            {
                closestPoint = new Double(sx2, sy2);
            }
            else
            {
                closestPoint = new Double((int) Math.round(sx1 + u * xDelta), (int) Math.round(sy1 + u * yDelta));
            }
            return closestPoint;
        }

        public Double getClosestPointOnSegment(Double a, Double b){ return getClosestPointOnSegment(a.x, a.y, b.x, b.y, x, y); }
        public Double getClosestPointOnSegment(Point2D a, Point2D b){ return getClosestPointOnSegment(a.getX(), a.getY(), b.getX(), b.getY(), x, y); }
        public Double getClosestPointOnSegment(Line2D l){ return getClosestPointOnSegment(l.getX1(), l.getY1(), l.getX2(), l.getY2(), x, y); }
        public void add(Coordinate.Double anotherDouble){
            this.x+=anotherDouble.x;
            this.y+=anotherDouble.y;
        }
        public void difference(Coordinate.Double anotherDouble){
            this.x-=anotherDouble.x;
            this.y-=anotherDouble.y;
        }
        public void minus(){
            x=-x;
            y=-y;
        }
        public void multiply(double v){
            this.x*=v;
            this.y*=v;
        }

        @Override
        public String toString() {
            return "Double{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }
}
