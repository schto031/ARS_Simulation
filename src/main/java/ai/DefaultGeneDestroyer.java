package ai;

import common.Utilities;

import java.util.Random;
import java.util.function.Supplier;

public class DefaultGeneDestroyer implements IGeneDestroyer {
    float mutationProbability=0.1f;
<<<<<<< HEAD
    float crossoverProbability=0.2f;
=======
    float crossoverProbability=0.3f;
>>>>>>> 07c86a8ac05e53788cf9a94a95ec8fddcace3289
    final Random random=new Random();
    public final Supplier<Double> randomizer=()->Utilities.rand(random, -10,10);

    public DefaultGeneDestroyer(float mutationProbability, float crossoverProbability) {
        this.mutationProbability = mutationProbability;
        this.crossoverProbability = crossoverProbability;
    }

    public DefaultGeneDestroyer(){}

    @Override
    public void mutate(RobotController nn) {
        for(var w:nn.weights){
            var data=w.getMatrix().data;
            for(var i=0;i<data.length;i++){
                if(random.nextDouble()<mutationProbability){ data[i]= randomizer.get(); }
            }
        }
    }

    @Override
    public void crossover(RobotController a, RobotController b) {
        validate(a,b);
        for(var i=0;i<a.weights.length;i++){
            var aa=a.weights[i].getMatrix().data;
            var bb=b.weights[i].getMatrix().data;
            for(var j=0;j<aa.length;j++){
                if(random.nextDouble()<crossoverProbability){
                    var tmp=aa[j];
                    aa[j]=bb[j];
                    bb[j]=tmp;
                }
            }
        }
    }

    void validate(RobotController a, RobotController b){
        if(a.weights.length!=b.weights.length) throw new IllegalArgumentException("Incompatible networks for crossover! "+a.weights.length+" vs "+b.weights.length);
        for(var i=0;i<a.weights.length;i++) {
            var aa = a.weights[i].getMatrix().data;
            var bb = b.weights[i].getMatrix().data;
            if (aa.length != bb.length)
                throw new IllegalArgumentException("Incompatible weights at layer " + i + "! " + aa.length + " vs " + bb.length);
        }
    }
}
