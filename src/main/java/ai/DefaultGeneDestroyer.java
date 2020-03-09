package ai;

import common.Utilities;

import java.util.Random;

public class DefaultGeneDestroyer implements IGeneDestroyer{
    private float mutationProbability=0.01f;
    private float crossoverProbability=0.5f;
    private Random random=new Random();

    public DefaultGeneDestroyer(float mutationProbability, float crossoverProbability) {
        this.mutationProbability = mutationProbability;
        this.crossoverProbability = crossoverProbability;
    }

    public DefaultGeneDestroyer(){}

    @Override
    public void mutate(RobotController nn) {
        var rand=new Random();
        for(var w:nn.weights){
            var data=w.getMatrix().data;
            for(var i=0;i<data.length;i++){
                if(random.nextDouble()<mutationProbability){
                    data[i]= Utilities.rand(rand,-5,5);
                }
            }
        }
    }

    @Override
    public void crossover(RobotController a, RobotController b) {
        if(a.weights.length!=b.weights.length) throw new IllegalArgumentException("Incompatible networks for crossover! "+a.weights.length+" vs "+b.weights.length);
        for(var i=0;i<a.weights.length;i++){
            var aa=a.weights[i].getMatrix().data;
            var bb=b.weights[i].getMatrix().data;
            if(aa.length!=bb.length) throw new IllegalArgumentException("Incompatible weights at layer "+i+"! "+aa.length+" vs "+bb.length);
            for(var j=0;j<aa.length;j++){
                if(random.nextDouble()<crossoverProbability){
                    var tmp=aa[j];
                    aa[j]=bb[j];
                    bb[j]=tmp;
                }
            }
        }
    }
}
