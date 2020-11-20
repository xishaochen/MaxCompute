import com.aliyun.odps.io.Text;
import com.aliyun.odps.io.Writable;
import com.aliyun.odps.udf.ExecutionContext;
import com.aliyun.odps.udf.UDFException;
import com.aliyun.odps.udf.Aggregator;
import com.aliyun.odps.udf.annotation.Resolve;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

// TODO define input and output types, e.g. "double->double".
@Resolve("string,string->string")
public class UDAFarrayString extends Aggregator {


    private static class MyBuffer implements Writable{
        private String groupName;
        private String column;
        @Override
        public void write(DataOutput out) throws IOException {
            out.writeUTF(groupName);
            out.writeUTF(column);
        }
        @Override
        public void readFields(DataInput in) throws IOException {
            groupName =  in.readUTF();
            column = in.readUTF();
        }
    }

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

    Map<String,String> map = new LinkedHashMap<>();
    Text t = new Text("");

    String str = "";
    /**
     * @param buffer 聚合buffer
     * @param args   SQL中调用UDAF时指定的参数，不能为null，但是args里面的元素可以为null，代表对应的输入数据是null
     * @throws UDFException
     */
    @Override
    public void iterate(Writable buffer, Writable[] args) throws UDFException {
        // TODO
        //获取分组字段和拼接字段
        String s = String.valueOf(args[0]);
        String s1 = String.valueOf(args[1]);
        MyBuffer buf = (MyBuffer) buffer;
        //判断map中是不是有新传入的值 有的话和之前拼接 没的话添加
        if (s!=null&&!s.equals("")){
            if (map.containsKey(s)){
                String s2 = map.get(s);
                t = new Text(s2+","+s1);
                map.put(s,String.valueOf(t));
            }
            else {
                map.put(s,s1);
            }
        }
        buf.groupName=s;
        buf.column=map.get(s);
    }

    private Text text = new Text();

    /**
     * @param buffer  聚合buffer
     * @param partial 分片聚合结果
     * @throws UDFException
     */
    @Override
    public void merge(Writable buffer, Writable partial) throws UDFException {
        // TODO
        MyBuffer buf = (MyBuffer) buffer;
        MyBuffer p = (MyBuffer) partial;
        buf.groupName = p.groupName;
        buf.column = p.column;
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
        MyBuffer buf1 = (MyBuffer) buffer;
        if (buf1.column.startsWith(",")){
            buf1.column=buf1.column.substring(1);
        }
        text.set("["+buf1.column+"]");
        return text;
    }

    @Override
    public void close() throws UDFException {

    }

}