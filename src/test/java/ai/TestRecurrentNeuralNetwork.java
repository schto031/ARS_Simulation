package ai;

import org.ejml.ops.MatrixFeatures;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestRecurrentNeuralNetwork {
    @Test
    public void testRNN1(){
        var rnn=new RecurrentNeuralNetwork(1,2,new RobotController.Relu(),1,10,1,1);
        var initialInputLayer=rnn.layers[0].copy();
        rnn.forwardPropagate();
        Assertions.assertTrue(MatrixFeatures.isEquals(rnn.layers[0].getMatrix(), initialInputLayer.getMatrix()));
        rnn.forwardPropagate();
        Assertions.assertTrue(MatrixFeatures.isEquals(rnn.layers[0].getMatrix(), initialInputLayer.getMatrix()));
        rnn.forwardPropagate();
        Assertions.assertFalse(MatrixFeatures.isEquals(rnn.layers[0].getMatrix(), initialInputLayer.getMatrix()));
    }

    @Test
    public void testRNNReference(){
        var rnn=new RecurrentNeuralNetwork(1,2,new RobotController.Relu(),1,10,1,1);
        var reference=new double[]{1};
        rnn.setInputByReference(reference);
        var initial=rnn.layers[0].copy();
        reference[0]=0;
        Assertions.assertFalse(MatrixFeatures.isEquals(initial.getMatrix(), rnn.layers[0].getMatrix()), "Value in input layer should be updated by reference");
    }

    @Test
    public void testRNNReference2(){
        var rnn=new RecurrentNeuralNetwork(1,2,new RobotController.Relu(),1,1,1,1);
        var reference=new double[]{1};
        rnn.setInputByReference(reference);
        var initial=rnn.layers[0].copy();
        System.out.println(initial);
        reference[0]=0;
        System.out.println(rnn.layers[0]);

        Assertions.assertFalse(MatrixFeatures.isEquals(initial.getMatrix(), rnn.layers[0].getMatrix()), "Value in input layer should be updated by reference");
    }
}
