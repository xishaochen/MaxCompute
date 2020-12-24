import com.aliyun.odps.udf.UDF;
import org.apache.commons.lang.ArrayUtils;

import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UDFinfoJson extends UDF {
    // TODO define parameters and return type, e.g:  public String evaluate(String a, String b)
    //传入参数 "姓名#性别#金额",xm,xb,je
    public String evaluate(String[] s) {
        String[] split = s[0].split("#");
        int i = split.length;
        String[] strings = new String[i];
        int j = 0;
        String str = "";
        for (int m = 0;m<i;m++){
            if (s[j+1]==null){
                s[j+1]="";
            }
            str+="\""+split[j]+"\""+":"+"\""+s[j+1]+"\""+",";
            j+=1;
        }
        str=str.substring(0,str.length()-1);
        return "{" + str + "}";
    }


}