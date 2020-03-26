import org.ejml.simple.SimpleMatrix;

import java.util.Random;

public class KalmanFilter {

    private final Random random=new Random();
    SimpleMatrix A, B, R, C, Q;
    private final Robo robo;
    private final int scalingFactor=4; // Scaling factor for random gaussian

    public KalmanFilter(final Robo robo){
        this.robo=robo;
        A=SimpleMatrix.identity(3); // Verified
        R=SimpleMatrix.identity(3).scale(1);    // Needs to be random?
        getValues();
        // For correction
        C=SimpleMatrix.identity(3); // verified
        Q=SimpleMatrix.identity(3).scale(100);    // S.D. of gaussian noise, here 1
    }

    public void getValues(){
        var dt=robo.dt.get()/1000;  // Convert to seconds
        var orientation=robo.orientation;
        B=new SimpleMatrix(3, 2, true, dt*Math.cos(orientation), 0, dt*Math.sin(orientation), 0, 0, dt);
        // Also, randomize R
        R=SimpleMatrix.diag(random.nextGaussian()*scalingFactor, random.nextGaussian()*scalingFactor, random.nextGaussian()*scalingFactor);
    }

    public Bundle predict(SimpleMatrix state, SimpleMatrix covariance, SimpleMatrix ut, SimpleMatrix zt){
        getValues();
        // Prediction
        var mu=A.mult(state).plus(B.mult(ut));
        var sigma=A.mult(covariance.mult(A.transpose())).plus(R);
        System.err.println("Mu\n"+mu);
        System.err.println("Sigma\n"+sigma);
        // Correction
        var K=sigma.mult(C.transpose()).mult((C.mult(sigma).mult(C.transpose()).plus(Q)).invert());
        var correctedMu=mu.plus(K.mult((zt.minus(C.mult(mu)))));
        var correctedSigma=(SimpleMatrix.identity(3).minus(K.mult(C))).mult(sigma);
        System.out.println("Correct Mu\n"+correctedMu);
        System.out.println("Correct Sigma\n"+correctedSigma);
        return new Bundle(mu, sigma, correctedMu, correctedSigma);
    }

    static class Bundle{
        public SimpleMatrix mu, sigma, correctedMu, correctedSigma;

        public Bundle(SimpleMatrix mu, SimpleMatrix sigma, SimpleMatrix correctedMu, SimpleMatrix correctedSigma) {
            this.mu = mu;
            this.sigma = sigma;
            this.correctedMu = correctedMu;
            this.correctedSigma = correctedSigma;
        }
    }
}
