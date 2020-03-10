package ai;
import org.ejml.simple.SimpleMatrix;

import java.io.*;
import java.util.UUID;
import java.util.function.Function;

public abstract class RobotController implements IRobotController, Cloneable, Serializable {
    SimpleMatrix[] layers;
    SimpleMatrix[] weights;
    transient final Activation activation;
    public UUID ID=UUID.randomUUID();

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

    public static class Sigmoid extends Activation{ public Sigmoid() { super((z)->1/(1+Math.exp(-z))); }}

    public static class ClippedRelu extends Activation{
        public ClippedRelu(double limit) {
            super((a)->{
                if(a>limit) a=limit;
                if(a<0) a=0d;
                return a;
            });
        }
    }

    public static class Relu extends Activation{ public Relu() { super((a)->a<0?0:a); }}

    public static class Tanh extends Activation{ public Tanh() { super(Math::tanh); }}

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

    public void dump() throws IOException{
        try(var fos=new FileOutputStream(System.getProperty("TRAINED","weights.obj"));
            var oos=new ObjectOutputStream(fos)){
            oos.writeObject(weights);
        }
    }

    public void setInputByValue(double ...values){
        for(var i=0;i<values.length;i++){
            layers[0].getMatrix().data[i]=values[i];
        }
    }
}
