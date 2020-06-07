import java.util.function.Function;

public interface IRobotMovement {
    void incrementLeftVelocity();
    void incrementRightVelocity();
    void decrementLeftVelocity();
    void decrementRightVelocity();
    void incrementBothVelocity();
    void decrementBothVelocity();
    void stop();
    void setPosition(double x, double y);
    void setVelocity(Function<Double, Double> left, Function<Double, Double> right);
    double[] getSensorValues();
}
