import com.aliyun.odps.udf.UDF;

import java.util.regex.Pattern;

public class MyFirstUdf extends UDF {
    // TODO define parameters and return type, e.g:  public String evaluate(String a, String b)
    public String evaluate(String s) {
        return "hello world:" + s;
    }
}