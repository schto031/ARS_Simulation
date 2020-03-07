package ai;

import org.ejml.simple.SimpleMatrix;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;

public class NeuralNetwork implements Cloneable {
    protected SimpleMatrix[] layers;
    protected SimpleMatrix[] weights;
    private final Activation activation;

    public NeuralNetwork(Activation activation, int... numberOfNodesPerLayer) {
        layers=new SimpleMatrix[numberOfNodesPerLayer.length];
        weights=new SimpleMatrix[numberOfNodesPerLayer.length-1];
        var rand=new Random();
        for(var i=0;i<numberOfNodesPerLayer.length;i++){
            var layer=SimpleMatrix.random(numberOfNodesPerLayer[i],1, -1,1, rand);
            layers[i]=layer;
        }
        for(var i=0;i<numberOfNodesPerLayer.length-1;i++){
            var weight=SimpleMatrix.random(numberOfNodesPerLayer[i], numberOfNodesPerLayer[i+1], -1,1, rand);
            weights[i]=weight;
        }
        this.activation=activation;
    }

    public NeuralNetwork(int... numberOfNodesPerLayer) { this(new Relu() ,numberOfNodesPerLayer); }

    private String describe(){
        StringBuilder sb=new StringBuilder();
        for(var i=0;i<layers.length-1;i++){
            sb.append(String.format("%d:%dx%d\n",i, layers[i].getMatrix().numRows, layers[i].getMatrix().numCols));
            sb.append(String.format("\t%dx%d\n", weights[i].getMatrix().numRows, weights[i].getMatrix().numCols));
        }
        sb.append(String.format("%d:%dx%d\n",layers.length-1, layers[layers.length-1].getMatrix().numRows, layers[layers.length-1].getMatrix().numCols));
        return sb.toString();
    }

    public void setInput(double ...values){
        for(var i=0;i<values.length;i++){
            layers[0].getMatrix().data[i]=values[i];
        }
    }

    public double[] getOutput(){ return layers[layers.length-1].getMatrix().getData(); }

    public void forwardPropagate(){
        for(var i=0;i<weights.length;i++){
            layers[i+1]=weights[i].transpose().mult(layers[i]);
            activation.activate(layers[i+1]);
        }
    }

    public double[] getGene(int layer){ return weights[layer].getMatrix().getData(); }

    protected int[] getConfiguration(){
        var config=new int[layers.length];
        for(var i=0;i<layers.length;i++) config[i]=layers[i].getMatrix().data.length;
        return config;
    }

    private abstract static class Activation{
        private Function<Double, Double> function;

        public Activation(Function<Double, Double> function) { this.function = function; }

        public SimpleMatrix activate(SimpleMatrix matrix){
            for(var i=0;i<matrix.getMatrix().data.length;i++){
                var data=matrix.getMatrix().data;
                data[i]=function.apply(data[i]);
            }
            return matrix;
        }
    }

    private static class Sigmoid extends Activation{
        public Sigmoid() { super((z)->1/(1+Math.exp(-z))); }
    }

    private static class Relu extends Activation{
        public Relu() { super((a)->a<0?0:a); }
    }

    private static class Tanh extends Activation{
        public Tanh() { super(Math::tanh); }
    }

    @Override
    protected NeuralNetwork clone() throws CloneNotSupportedException {
        return new NeuralNetwork(activation, getConfiguration());
    }

    @Override
    public String toString() {
        return "ai.NeuralNetwork{" +
                "layers=" + Arrays.toString(layers) +
                ", weights=" + Arrays.toString(weights) +
                '}'+"\n"+describe();
    }
}
