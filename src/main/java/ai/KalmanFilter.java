package ai;

import common.Coordinate;
import org.ejml.simple.SimpleMatrix;

import java.util.Arrays;

public class KalmanFilter {
    // x, y, phi
    private final int numberOfStateVariables=3;
    SimpleMatrix X,Control,A,C,Q,R,covariance;
    long previousTime=System.currentTimeMillis();
    double dt=0;

    public KalmanFilter(Coordinate.Double initialPosition, double initialOrientation) {
        X=new SimpleMatrix(1, numberOfStateVariables, false, initialPosition.x, initialPosition.y, initialOrientation);
        var assumedInitialCovarianceValues=5;
        var diagonalValues=new int[numberOfStateVariables];
        Arrays.fill(diagonalValues, assumedInitialCovarianceValues);
        covariance=SimpleMatrix.diag(assumedInitialCovarianceValues);
        R=SimpleMatrix.identity(numberOfStateVariables);
        Q=new SimpleMatrix(numberOfStateVariables, numberOfStateVariables);
        Q.plus(0.1);
    }

    public void update(){
        var curTime=System.currentTimeMillis();
        dt=curTime-previousTime;
        previousTime=curTime;
    }
}
