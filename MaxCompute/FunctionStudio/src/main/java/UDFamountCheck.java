import com.aliyun.odps.udf.UDF;

import java.util.regex.Matcher;

/**
 * 将有千分位逗号的清洗为无千分位，小数点后两位没有则默认填充0.00.
 * 小数点超过两位数的为校验失败
 * 中文或特殊符号也为校验失败
 * 小数点前超过11位数为校验失败
 */
public class UDFamountCheck extends UDF {

    public String evaluate(String s) {
        //处理空字符串和空值
        if (s == null || s.equals("")) {
            return null;
        }

        //如果存在中文，非千分位逗号，小数点的字符，输出到问题表
        if (!s.matches("[0-9.,]{1,}")) {
            return "@" + s;
        }

        //清洗字符串，只保留数字和小数点
        String amount = s.replaceAll("[^0-9.]*", "");

        String[] amounts = amount.split("\\.");

        if (amounts.length == 1 && amounts[0].length() <= 11) {
            return amounts[0] + ".00";
        }

        //判断整数位位数
        if (amounts[0].length() > 11 || amounts[1].length() > 2) {
            return "@" + s;
        }
        return String.format("%.2f", Double.parseDouble(amount));
    }
}