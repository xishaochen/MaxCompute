import com.aliyun.odps.udf.UDF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class UDFarraySplit extends UDF {
    // TODO define parameters and return type, e.g:  public String evaluate(String a, String b)
    public String evaluate(String s1) {
        if(s1==null || s1.equals("")){
            s1="";
        }
        //转为list集合
        ArrayList<String> list = new ArrayList<>();
        for (String s : s1.split(",")) {
            if (s != null && s.length() != 0){
                list.add(s);
            }
        }
        //转为set集合
        HashSet<String> set = new HashSet<>(list);
        //set集合转为字符串数组
        String[] arrays = set.toArray(new String[0]);
        //数组转化为字符串
        String s = Arrays.toString(arrays);
        return s.substring(1, s.length() - 1);
    }
}