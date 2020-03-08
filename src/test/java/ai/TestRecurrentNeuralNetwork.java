package ai;

import org.ejml.ops.MatrixFeatures;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestRecurrentNeuralNetwork {
    @Test
    public void testRNN(){
        System.out.println("testing RNN");
        var rnn=new RecurrentNeuralNetwork(1,2,new NeuralNetwork.Relu(),1,10,1,1);
        var initialInputLayer=rnn.layers[0].copy();
        rnn.forwardPropagate();
        Assertions.assertTrue(MatrixFeatures.isEquals(rnn.layers[0].getMatrix(), initialInputLayer.getMatrix()));
        rnn.forwardPropagate();
        Assertions.assertTrue(MatrixFeatures.isEquals(rnn.layers[0].getMatrix(), initialInputLayer.getMatrix()));
        rnn.forwardPropagate();
        Assertions.assertFalse(MatrixFeatures.isEquals(rnn.layers[0].getMatrix(), initialInputLayer.getMatrix()));
    }
}
