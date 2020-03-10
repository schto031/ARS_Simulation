package ai;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestNeuralNetwork {
    @Test
    public void testNN(){
        System.out.println("Hello");
        var numberOfSensorsNodes=12;
        var numberOfHiddenNodes=4;
        var numberOfOutputNodes=2;
        var numberOfInputNodes=numberOfSensorsNodes+numberOfHiddenNodes;
        var nn=new NeuralNetwork(numberOfInputNodes, numberOfHiddenNodes, numberOfOutputNodes);
        nn.forwardPropagate();
    }

    @Test
    public void testFP(){
        var nn=new NeuralNetwork(1,1);
        nn.layers[0].getMatrix().data[0]=0;
        nn.forwardPropagate();
        Assertions.assertEquals(nn.layers[1].getMatrix().data[0]+0.0, 0);
        nn.layers[0].getMatrix().data[0]=-10;
        nn.weights[0].getMatrix().data[0]=1;
        nn.forwardPropagate();
        Assertions.assertEquals(nn.layers[1].getMatrix().data[0]+0.0, 0);
    }
}
