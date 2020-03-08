package common;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

public class Utilities {
    public static double rand(Random random, double min, double max){ return random.nextDouble()*(max-min)-min; }
    public static Point2D rand(Random random, Rectangle2D bounds){ return new Point2D.Double(
            rand(random, bounds.getMinX(), bounds.getMaxX()),
            rand(random, bounds.getMinY(), bounds.getMaxY()));
    }
}
