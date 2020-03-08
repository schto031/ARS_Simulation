package ai;

import org.ejml.simple.SimpleMatrix;

import java.util.function.Function;

public abstract class RobotController implements IRobotController {
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
}
