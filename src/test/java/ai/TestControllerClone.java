package test.java.ai;

import org.ejml.ops.MatrixFeatures;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestControllerClone {
    @Test
    public void testNNClone() throws CloneNotSupportedException {
        var nn=new NeuralNetwork(2,2,2,2);
        var nn2=nn.clone();
        System.out.println(nn.weights[0]);
        Assertions.assertEquals(nn.layers.length, nn2.layers.length);
        Assertions.assertTrue(MatrixFeatures.isEquals(nn.weights[0].getMatrix(), nn2.weights[0].getMatrix()));
        nn.weights[0]=new SimpleMatrix(nn.weights[0].numRows(), nn.weights[0].numCols());
        System.out.println(nn2.weights[0]);
        Assertions.assertFalse(MatrixFeatures.isEquals(nn.weights[0].getMatrix(), nn2.weights[0].getMatrix()));
    }
}
