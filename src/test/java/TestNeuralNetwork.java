import org.junit.jupiter.api.Test;

public class TestNeuralNetwork {
    @Test
    public void testNN(){
        System.out.println("Hello");
        var numberOfSensorsNodes=12;
        var numberOfHiddenNodes=4;
        var numberOfOutputNodes=2;
        var numberOfInputNodes=numberOfSensorsNodes+numberOfHiddenNodes;
        var nn=new NeuralNetwork(numberOfInputNodes, numberOfOutputNodes, numberOfHiddenNodes);
        System.out.println(nn.layers[1]);
        nn.forwardPropagate();

    }
}
