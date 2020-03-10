import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Evaluation {
    public static List<Robo> getBestBots(Robo[] robots, int number){
        return Arrays.stream(robots).sorted(Comparator.comparingDouble(Evaluation::evaluationFunction).reversed()).limit(number).collect(Collectors.toUnmodifiableList());
    }

    private static final double APPRECIATE_FACTOR=1;
    private static final double COLLISION_PENAL_FACTOR=5;
    private static final double ROTATION_PENAL_FACTOR=0;

    public static double evaluationFunction(Robo robo){
        return robo.getCollectedDust()*APPRECIATE_FACTOR
                -robo.collisions.get()*COLLISION_PENAL_FACTOR
                -Math.abs(robo.getDifferentialVelocity())*ROTATION_PENAL_FACTOR;
    }
}
