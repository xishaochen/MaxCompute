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

    /**
     * 简单清洗联系号码
     *
     * @param number String
     * @return String
     */
    private static String numClean(String number) {

        if (!number.matches("[0-9\\-]+")) {
            return number.replaceAll("[^\\d\\-]", "");
        }
        return number;
    }

    // TODO define parameters and return type, e.g:  public String evaluate(String a, String b)
    public String evaluate(String s) {
        //处理空值
        if (s == null || s.equals("")) {
            return null;
        }

        String newNum;
        if (s.contains(".")) {
            newNum = s.split("\\.")[0];
        } else {
            newNum = s;
        }
        String result = numClean(newNum);
        //校验手机号是否为1开头，3,4,5,7,8为第二位，
        if (result.length() == 11 && result.matches("[1]{1}[345789]{1}\\d{9}$")) {
            return result;
        }

        // 校验座机 填区号
        if (result.length() == 11 || result.length() == 12 || result.length() == 13 || result.length() == 14) {
            if (result.matches("0\\d{2}-[2-8]{1}\\d{7}") || result.matches("0\\d{3}-[2-8]{1}\\d{7}") || result.matches("0\\d{2}-[2-8]{1}\\d{6}") || result.matches("0\\d{3}-[2-8]{1}\\d{6}")) {
                return result;
            }
        }

        //未填区号
        if ((result.length() == 7 && result.matches("[2-8]{1}\\d{6}")) || (result.length() == 8 && result.matches("[2-8]{1}\\d{7}"))) {
            return result;
        }
        return "11@" + result;
    }
}