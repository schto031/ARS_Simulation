package ai;

public class SinglePointGeneDestroyer extends DefaultGeneDestroyer implements IGeneDestroyer {
    @Override
    public void mutate(RobotController nn) {
        var layer=nn.weights[random.nextInt(nn.weights.length)];
        var data=layer.getMatrix().data;
        data[random.nextInt(data.length)]=randomizer.get();
    }
}
