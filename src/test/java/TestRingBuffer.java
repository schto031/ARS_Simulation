package java;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.junit.jupiter.api.Test;

public class TestRingBuffer {
    @Test
    public void testBuffer(){
        var buf=new CircularFifoQueue<Integer>(10);
        for(var i=0;i<20;i++){
            buf.add(i);
//            System.out.println(buf);
//            if(buf.isAtFullCapacity()) System.out.println(buf.peek());
        }
    }
}
