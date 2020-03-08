package ai;

import org.ejml.simple.SimpleMatrix;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

public class NeuralNetwork extends RobotController implements Cloneable, IRobotController {
        public NeuralNetwork(Activation activation, int... numberOfNodesPerLayer) {
        super(activation);
        initializeRandomArray(numberOfNodesPerLayer);
    }

    public NeuralNetwork(int... numberOfNodesPerLayer) { this(new Relu() ,numberOfNodesPerLayer); }
    NeuralNetwork(Activation activation){ super(activation); }

    void initializeRandomArray(int... numberOfNodesPerLayer){
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
    }

    private String describe(){
        StringBuilder sb=new StringBuilder();
        for(var i=0;i<layers.length-1;i++){
            sb.append(String.format("%d:%dx%d\n",i, layers[i].getMatrix().numRows, layers[i].getMatrix().numCols));
            sb.append(String.format("\t%dx%d\n", weights[i].getMatrix().numRows, weights[i].getMatrix().numCols));
        }
        sb.append(String.format("%d:%dx%d\n",layers.length-1, layers[layers.length-1].getMatrix().numRows, layers[layers.length-1].getMatrix().numCols));
        return sb.toString();
    }

    public void setInputByValue(double ...values){
        for(var i=0;i<values.length;i++){
            layers[0].getMatrix().data[i]=values[i];
        }
    }

    @Override
    public double[] getInput() {
        return layers[0].getMatrix().data;
    }

    public void setInputByReference(double ...values){
        if(values.length!=getInput().length) throw new IllegalArgumentException(String.format("Input layer dimensions must match! %d vs %d",values.length,getInput().length));
        layers[0].getMatrix().data=values;
    }

    @Override
    public double[] getOutput(){ return layers[layers.length-1].getMatrix().getData(); }

    @Override
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

    @Override
    protected NeuralNetwork clone() throws CloneNotSupportedException {
        return new NeuralNetwork(activation, getConfiguration());
    }

    public void dump(String filename) throws IOException {
        var dumpLocation=System.getProperty("DUMP_LOCATION")+"/"+filename;
        try(var fos=new FileOutputStream(dumpLocation); var oos=new ObjectOutputStream(fos)){
            oos.writeObject(this);
        }
    }

    public static NeuralNetwork load(String filename) throws IOException, ClassNotFoundException {
        var dumpLocation=System.getProperty("DUMP_LOCATION")+"/"+filename;
        try(var fis=new FileInputStream(dumpLocation); var oos=new ObjectInputStream(fis)){
            return (NeuralNetwork) oos.readObject();
        }
    }

    @Override
    public String toString() {
        return "ai.NeuralNetwork{" +
                "layers=" + Arrays.toString(layers) +
                ", weights=" + Arrays.toString(weights) +
                '}'+"\n"+describe();
    }
}
