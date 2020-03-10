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
        var initial=rnn.layers[0].copy();
        reference=rnn.setInputByReference(reference);
        reference[0]=0;
        Assertions.assertFalse(MatrixFeatures.isEquals(initial.getMatrix(), rnn.layers[0].getMatrix()), "Value in input layer should be updated by reference");
    }

    @Test
    public void testRNNReference2(){
        var rnn=new RecurrentNeuralNetwork(1,2,new RobotController.Relu(),1,1,1,1);
        var reference=new double[]{1};
        var initial=rnn.layers[0].copy();
        rnn.setInputByReference(reference);
        reference[0]=0;
        Assertions.assertFalse(MatrixFeatures.isEquals(initial.getMatrix(), rnn.layers[0].getMatrix()), "Value in input layer should be updated by reference");
    }

    @Test
    public void testFP(){
        var nn=new RecurrentNeuralNetwork(1,2,new RobotController.Relu(),1,1);
        nn.layers[0].getMatrix().data[0]=0;
        for(var i=0;i<10;i++){
            Assertions.assertEquals(nn.layers[0].getMatrix().data[0]+0.0, 0);
            nn.forwardPropagate();
        }
        nn=new RecurrentNeuralNetwork(1,2,new RobotController.Relu(),1,1);
        nn.layers[0].getMatrix().data[0]=1;
        nn.weights[0].getMatrix().data=new double[]{1,1};

        for(var i=0;i<10;i++){
            nn.forwardPropagate();
            System.out.println(nn);
        }
    }
}
