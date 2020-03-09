package ai;
import org.ejml.simple.SimpleMatrix;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.function.Function;

public abstract class RobotController implements IRobotController, Cloneable {
    SimpleMatrix[] layers;
    SimpleMatrix[] weights;
    final Activation activation;

    RobotController(Activation activation) { this.activation = activation; }

    abstract static class Activation{
        private Function<Double, Double> function;
        public Activation(Function<Double, Double> function) { this.function = function; }

        public SimpleMatrix activate(SimpleMatrix matrix){
            for(var i=0;i<matrix.getMatrix().data.length;i++){
                var data=matrix.getMatrix().data;
                data[i]=function.apply(data[i]);
            }
            return matrix;
        }
    }

    static class Sigmoid extends Activation{ public Sigmoid() { super((z)->1/(1+Math.exp(-z))); }}

    static class Relu extends Activation{ public Relu() { super((a)->a<0?0:a); }}

    static class Tanh extends Activation{ public Tanh() { super(Math::tanh); }}

    @Override
    public RobotController clone() throws CloneNotSupportedException {
        var c=(RobotController) super.clone();
        c.layers=new SimpleMatrix[c.layers.length];
        c.weights=new SimpleMatrix[c.weights.length];
        for(var i=0;i<layers.length;i++){ c.layers[i]=layers[i].copy(); }
        for(var i=0;i<weights.length;i++){ c.weights[i]=weights[i].copy(); }
        return c;
    }

    public void toFile() throws IOException {
        try(var fos=new FileOutputStream("o.log", true);
        var ps=new PrintStream(fos)){
            System.setOut(ps);
            System.out.println(this);
        } finally {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        }
    }
}
