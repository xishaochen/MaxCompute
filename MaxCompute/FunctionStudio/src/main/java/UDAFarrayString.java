import com.aliyun.odps.io.Text;
import com.aliyun.odps.io.Writable;
import com.aliyun.odps.udf.ExecutionContext;
import com.aliyun.odps.udf.UDFException;
import com.aliyun.odps.udf.Aggregator;
import com.aliyun.odps.udf.annotation.Resolve;
import org.apache.commons.lang.StringUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

// TODO define input and output types, e.g. "double->double".
@Resolve({"string->string"})
public class UDAFarrayString extends Aggregator {

    private static class MyBuffer implements Writable{
        private String sum;
        @Override
        public void write(DataOutput out) throws IOException {
            out.writeUTF(sum);
        }
        @Override
        public void readFields(DataInput in) throws IOException {
            sum =  in.readUTF();
        }
    }

    private Text t = new Text();

    @Override
    public void setup(ExecutionContext ctx) throws UDFException {

    }

    /**
     * 创建聚合Buffer
     *
     * @return Writable聚合buffer
     */
    @Override
    public Writable newBuffer() {
        // TODO
        return new MyBuffer();
    }
    //ArrayList<String> strings = new ArrayList<>();
    /**
     * @param buffer 聚合buffer
     * @param args   SQL中调用UDAF时指定的参数，不能为null，但是args里面的元素可以为null，代表对应的输入数据是null
     * @throws UDFException
     */
    @Override
    public void iterate(Writable buffer, Writable[] args) throws UDFException {
        Text arg  = (Text) args[0];
        MyBuffer myBuffer = (MyBuffer) buffer;
        if (arg!=null){
            myBuffer.sum += ","+ arg;
        }
    }

    /**
     * @param buffer  聚合buffer
     * @param partial 分片聚合结果
     * @throws UDFException
     */
    @Override
    public void merge(Writable buffer, Writable partial) throws UDFException {
        // TODO
        MyBuffer m = (MyBuffer) buffer;
        MyBuffer n  = (MyBuffer) partial;
        m.sum += ","+ n.sum;
    }

    /**
     * 生成最终结果
     *
     * @param buffer
     * @return Object UDAF的最终结果
     * @throws UDFException
     */
    @Override
    public Writable terminate(Writable buffer) throws UDFException {
        // TODO
        MyBuffer myBuffer = (MyBuffer) buffer;
        String sum = myBuffer.sum;
        sum ="["+sum.substring(10)+"]";
        return new Text(sum);
    }

    @Override
    public void close() throws UDFException {

    }

}