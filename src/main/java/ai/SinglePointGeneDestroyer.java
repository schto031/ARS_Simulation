package ai;

public class SinglePointGeneDestroyer extends DefaultGeneDestroyer implements IGeneDestroyer {
    @Override
    public void mutate(RobotController nn) {
        var layer=nn.weights[random.nextInt(nn.weights.length)];
        var data=layer.getMatrix().data;
        data[random.nextInt(data.length)]=randomizer.get();
    }

//    @Override
//    public void crossover(RobotController a, RobotController b) {
//        var x= Arrays.stream(a.weights).mapToInt(w->w.getMatrix().data.length).sum();
//        var crossoverPoint=x/2;
//        var unluckyWeightLayer=0;
//        for(int i=0, j=0;i<a.weights.length;i++, j+=a.weights[i].getMatrix().data.length){
//
//        }
//    }
}
