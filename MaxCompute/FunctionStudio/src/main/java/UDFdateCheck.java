import com.aliyun.odps.udf.UDF;

/**
 * 日期校验 UDFdateCheck
 * 格式化后 yyyy-mm-dd
 * 校验
 * 年份>1900
 * 月份 （1，12）
 * 日期  （各个月份的最大日期）
 */
public class UDFdateCheck extends UDF {

    /**
     *
     * @param year int
     * @param month int
     * @param day int
     * @return String
     */
    private String dateFormat(int year,int month,int day) {
        if (year <1900 || month > 12 || day > 31) {
            return "12@" + year + "-" + month + "-" + day;
        }
        if (month < 10 && day < 10) {
            return year + "-0" + month + "-0" + day;
        }
        if (month > 10 && day < 10) {
            return year + "-" + month + "-0" + day;
        }
        if (month < 10 && day > 10) {
            return year + "-0" + month + "-" + day;
        }
        return year + "-" + month + "-" + day;
    }
    // TODO define parameters and return type, e.g:  public String evaluate(String a, String b)
    public String evaluate(String s) {
        //处理空值
        if (s == null || "".equals(s)) {
            return "12@";
        }

        //轻度清洗，去除中文
        String replaceDate = s.replaceAll("[^0-9]+", "-");
        String[] split = replaceDate.split("-");

        if (split.length > 3 || split[0].length() < 4 || Integer.parseInt(split[0].substring(0, 4)) < 1900) {
            return "12@" + s;
        }

        //判断字符串是否存在非数字字符
        if (!replaceDate.matches("[0-9]+")) {
            if (split.length == 3) {
                int year = Integer.parseInt(split[0]);
                int month = Integer.parseInt(split[1]);
                int day = Integer.parseInt(split[2]);
                return dateFormat(year,month,day);
            }
            return "12@" + s;
        }
        if (split.length == 1 && split[0].length() == 8){ //纯数字类型的日期字段格式化
            int year = Integer.parseInt(split[0].substring(0,4));
            int month = Integer.parseInt(split[0].substring(4,6));
            int day = Integer.parseInt(split[0].substring(6,8));
            return dateFormat(year,month,day);
        }
        return "12@" + s;
    }
}