import com.aliyun.odps.udf.UDF;

public class UDFinfoJson extends UDF {
    // TODO define parameters and return type, e.g:  public String evaluate(String a, String b)
    //传入参数 "姓名#性别#金额",xm,xb,je
    public String evaluate(String[] s) {
        //数组长度
        int i = 0;
        //循环要用的下标
        int j = 0;
        //拼接字符串
        String str = "";
        //创建字段描述的数组
        ;        if (s[0]!=null&&!s[0].equals("")){
            String[] split = s[0].split("#");
            i = split.length;
            String[] strings = new String[i];
            for (int m = 0;m<i;m++){
                //判断传入字段值是不是空
                if (split[j]==null){
                    split[j]="";
                }
                if (split[j+1]==null){
                    split[j+1]="";
                }
                str+="\""+split[j]+"\""+":"+"\""+s[j+1]+"\""+",";
                j+=1;
            }
        }
        str=str.substring(0,str.length()-1);
        return "{" + str + "}";
    }
}