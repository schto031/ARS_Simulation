package ai;

public interface IGeneDestroyer {
    void mutate(RobotController nn);
    void crossover(RobotController a, RobotController b);
}
