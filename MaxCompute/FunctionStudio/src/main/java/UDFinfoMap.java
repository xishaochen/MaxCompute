import com.aliyun.odps.udf.UDF;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UDFinfoMap extends UDF {
    @Test
    public String evaluate(String s1, String s2) {
        // 切分为数组

        List<String> l1 = Arrays.asList(s1.split(","));
        List<String> l2 = Arrays.asList(s2.split(","));
        int i1 = l1.size();
        int i2 = l2.size();

        // 得到最大的一个值
        int max=i1;
        if(i2>max) max=i2;

        // 判空，则返回原始map s1:s2
        if(i1==0 && i2== 0 ){
            System.out.println("12");
            return s1+":"+s2;
        }

        // 拼接结果
        String res = "";
        for (int i = 0;i<max;i++){
            String k = "";
            String v = "";
            if(i < i1){
                k = l1.get(i);
            }
            if(i < i2){
                v = l2.get(i);
            }
            String str = k+":"+v;

            //如果是结尾则无需加中间符号
            if(i==max-1){
                res = res + str;
            } else{ res = res + str + "#";}
        }
        return res;
    }


}