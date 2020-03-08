package ai;

public interface IRobotController {
    double[] getInput();
    double[] getOutput();
    void forwardPropagate();
}
