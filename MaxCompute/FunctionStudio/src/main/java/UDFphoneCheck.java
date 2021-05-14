import com.aliyun.odps.udf.UDF;


import static java.util.regex.Pattern.*;

/**
 * 手机号校验
 * 1.位数是否等于11位。
 * 2.号码是否符合正则表达式/^1[3456789]\d{9}$/。
 * 电话号校验
 * 1、有区号 区号首位为0，区号位数为2位或4位，主体为7位或8位，主体首位不为0,1,9
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
        if (s == null || "".equals(s)) {
            return null;
        }

        //部分数据填入excel格式为小数，去除小数点后部分
        String newNum;
        if (s.contains(".")) {
            newNum = s.split("\\.")[0];
        } else {
            newNum = s;
        }

        //只保留所有数字及-，非数字被替换成" "
        String result = numClean(newNum);

        //如果去除非数字部分为空串，则返回
        if ("".equals(result)){
            return s;
        }

        //校验手机号是否为1开头，3,4,5,7,8为第二位，
        if (result.length() == 11 && result.matches("[1]{1}[345789]{1}\\d{9}$")) {
            return result;
        }

        // 校验座机,7 8位为未填区号，11，12，13，14位为区号,位数满足即可
        if (result.length() == 7 || result.length() == 8 || result.length()==11 || result.length() == 12 || result.length() == 13 || result.length() == 14) {
            return result;
        }
        return "11@" + result;
    }

}