package ai;

import org.ejml.simple.SimpleMatrix;

public abstract class RobotController implements IRobotController {
    SimpleMatrix[] layers;
    SimpleMatrix[] weights;
    final NeuralNetwork.Activation activation;
    protected RobotController(NeuralNetwork.Activation activation) { this.activation = activation; }
}
