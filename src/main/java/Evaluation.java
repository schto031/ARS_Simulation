package main.java;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Evaluation {
    public static List<Robo> getBestBots(Robo[] robots, int number){
        return Arrays.stream(robots).sorted(Comparator.comparingInt(Robo::getCollectedDust)).limit(number).collect(Collectors.toUnmodifiableList());
    }
}
