import org.ejml.simple.SimpleMatrix;

import java.util.Arrays;
import java.util.Random;

public class NeuralNetwork {
    protected SimpleMatrix[] layers;
    protected SimpleMatrix[] weights;

    public NeuralNetwork(int... numberOfNodesPerLayer) {
        layers=new SimpleMatrix[numberOfNodesPerLayer.length];
        weights=new SimpleMatrix[numberOfNodesPerLayer.length-1];
        var rand=new Random();
        for(var i=0;i<numberOfNodesPerLayer.length;i++){
            var layer=SimpleMatrix.random(numberOfNodesPerLayer[i],1, 0,1, rand);
            layers[i]=layer;
        }
        for(var i=0;i<numberOfNodesPerLayer.length-1;i++){
            var weight=SimpleMatrix.random(numberOfNodesPerLayer[i], numberOfNodesPerLayer[i+1], 0,1, rand);
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

    public void forwardPropagate(){
        for(var i=0;i<weights.length;i++){
            layers[i+1]=weights[i].transpose().mult(layers[i]);
        }
    }

    @Override
    public String toString() {
        return "NeuralNetwork{" +
                "layers=" + Arrays.toString(layers) +
                ", weights=" + Arrays.toString(weights) +
                '}'+"\n"+describe();
    }
}
