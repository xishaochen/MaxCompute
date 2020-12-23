import com.aliyun.odps.udf.UDF;

/**
 * 手机号校验
 * 1.位数是否等于11位。
 * 2.号码是否符合正则表达式/^1[3456789]\d{9}$/。
 * 电话号校验
 * 1、有区号 区号首位为0，区号位数为2维或4位，主体为7位或8位，主体首位不为0,1,9
 * 2、无区号 主体为7位或8位，主体首位不为0,1,9
 */
public class UDFphoneCheck extends UDF {
    // TODO define parameters and return type, e.g:  public String evaluate(String a, String b)
    public String evaluate(String s) {
        //处理空值
        if (s == null || s.equals("")) {
            return "";
        }

        //校验手机号是否为1开头，3,4,5,7,8为第二位，
        if (s.length() == 11 && s.matches("[1][345789][0-9]\\d{9}$")) {
            return  s;
        }

        // 校验座机
        if (s.length() == 11 || s.length() == 10 || s.length() == 13 || s.length() == 12) {
            if (s.matches("[0]\\d{2}[2-8]{1}\\d{7}") || s.matches("[0]\\d{3}[2-8]{1}\\d{7}") || s.matches("[0]\\d{2}[2-8]{1}\\d{6}") || s.matches("[0]\\d{3}[2-8]{1}\\d{6}")) {
                return s;
            }
        }
        return "11@" + s;
    }
}