import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class TestNeuralNetwork {
    @Test
    public void testNN(){
        System.out.println("Hello");
        var m=new SimpleMatrix(2,2);
        System.out.println(m);

        var numberOfSensorsNodes=12;
        var numberOfHiddenNodes=4;
        var numberOfOutputNodes=2;
        var numberOfInputNodes=numberOfSensorsNodes+numberOfHiddenNodes;
        var inputLayer=new SimpleMatrix(numberOfInputNodes,1);
        System.out.println(inputLayer);
        var hiddenLayer=new SimpleMatrix(numberOfHiddenNodes,1);
        System.out.println(hiddenLayer);
        var outputLayer=new SimpleMatrix(numberOfOutputNodes,1);
        var rand=new Random();
        var w12=SimpleMatrix.random(numberOfInputNodes, numberOfHiddenNodes,0,1,rand);
        var w23=SimpleMatrix.random(numberOfHiddenNodes, numberOfOutputNodes,0,1, rand);
        System.out.println(w12);
        System.out.println(w23);
        System.out.println(inputLayer.transpose().mult(w12));
    }
}
