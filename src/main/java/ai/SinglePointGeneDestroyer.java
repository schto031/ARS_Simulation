package ai;

import common.Utilities;

import java.util.Random;

public class SinglePointGeneDestroyer extends DefaultGeneDestroyer implements IGeneDestroyer {

    @Override
    public void mutate(RobotController nn) {
        var rand=new Random();
        var data=nn.weights.length;

    }
}
