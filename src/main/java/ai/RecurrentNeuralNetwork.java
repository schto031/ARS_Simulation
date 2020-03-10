package ai;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.ejml.simple.SimpleMatrix;

import java.util.Arrays;

public class RecurrentNeuralNetwork extends NeuralNetwork implements Cloneable, IRobotController {
    private int recurrentLayer;
    private final CircularFifoQueue<SimpleMatrix> previousValues;
    private int trueInputLayerSize;

    @Override
    void initializeRandomArray(int... numberOfNodesPerLayer) {
        numberOfNodesPerLayer[0]+=numberOfNodesPerLayer[recurrentLayer];
        super.initializeRandomArray(numberOfNodesPerLayer);
    }

    public RecurrentNeuralNetwork(int recurrentLayer, int bufferSize,NeuralNetwork.Activation activation, int... numberOfNodesPerLayer) {
        super(activation);
        this.recurrentLayer=recurrentLayer;
        if(recurrentLayer==0) throw new IllegalArgumentException("Cannot have a recurrent connection to input layer!");
        if(recurrentLayer>=numberOfNodesPerLayer.length) throw new IllegalArgumentException(
                String.format("Cannot have a recurrent connection to layer %d! Permitted values 1-%d!", recurrentLayer, numberOfNodesPerLayer.length-1));
        previousValues=new CircularFifoQueue<>(bufferSize);
        trueInputLayerSize=numberOfNodesPerLayer[0];
        initializeRandomArray(numberOfNodesPerLayer);
    }

    public RecurrentNeuralNetwork(int recurrentLayer, int bufferSize, int... numberOfNodesPerLayer) {
       this(recurrentLayer, bufferSize, new Relu(), numberOfNodesPerLayer);
    }

    public RecurrentNeuralNetwork(int recurrentLayer,int... numberOfNodesPerLayer) { this(recurrentLayer,10, new Relu(), numberOfNodesPerLayer); }

    public RecurrentNeuralNetwork(int... numberOfNodesPerLayer) {
        this(1, numberOfNodesPerLayer);
    }

    @Override
    public void forwardPropagate() {
        synchronized (previousValues){
            if(previousValues.isAtFullCapacity() && null!=layers[0] && previousValues.peek() != null) {
                layers[0].insertIntoThis(trueInputLayerSize, 0, previousValues.peek());
            }
            previousValues.add(layers[recurrentLayer].copy());
        }
        super.forwardPropagate();
    }

    @Override
    public double[] setInputByReference(double... values) {
        values= Arrays.copyOf(values, getInput().length);
//        var dgd=new DefaultGeneDestroyer();
//        for(var i=trueInputLayerSize;i<getInput().length;i++) values[i]=dgd.randomizer.get();
        super.setInputByReference(values);
        return values;
    }
}