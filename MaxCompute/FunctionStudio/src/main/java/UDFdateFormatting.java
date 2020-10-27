import com.aliyun.odps.udf.UDF;

public class UDFdateFormatting extends UDF {
    // TODO define parameters and return type, e.g:  public String evaluate(String a, String b)
    public String evaluate(String dateStr) {

        //轻度清洗，去除中文
        String replaceDate = dateStr.replaceAll("[^0-9]", "-");
        String[] split = replaceDate.split("-");
        String result = "";

        //判断字符串是否存在非数字字符
        if (!replaceDate.matches("[0-9]+")) {
            if (split.length > 3 || split.length < 1) {
                return null;
            } else if (split.length == 1 && split[0] != "") {
                result = split[0] + "-01-01";
            } else if (split.length == 2) {
                if (split[0].length() == 4) {
                    if (split[1].length() == 1) {
                        result = split[0] + "-0" + split[1] + "-01";
                    } else {
                        result = split[0] + "-" + split[1] + "-01";
                    }
                } else {
                    return null;
                }
            } else {
                if (split[0].length() == 4 && split[1].length() != 0 && split[2].length() != 0) {
                    if (split[1].length() == 1) {
                        if (split[2].length() == 1) {
                            result = split[0] + "-0" + split[1] + "-0" + split[2];
                        } else {
                            result = split[0] + "-0" + split[1] + "-" + split[2];
                        }
                    } else {
                        if (split[2].length() == 1) {
                            result = split[0] + "-" + split[1] + "-0" + split[2];
                        } else {
                            result = split[0] + "-" + split[1] + "-" + split[2];
                        }
                    }
                } else {
                    return null;
                }
            }
        } else { //纯数字类型的日期字段格式化
            String date = split[0];
            if (date.length() == 4 && Integer.parseInt(date) > 1970) {
                result = date + "-01-01";
            } else if (date.length() == 5) {
                result = date.substring(0,4) + "-0" + date.substring(4) + "-01";
            } else if (date.length() == 6) {
                result = date.substring(0,4) + "-" + date.substring(4,6) + "-01";
            } else if (date.length() == 7) {
                if (Integer.parseInt(date.substring(4,6)) > 12) {
                    result = date.substring(0,4) + "-0" + date.substring(4) + "-" + date.substring(5,7);
                } else {
                    result = date.substring(0,4) + "-" + date.substring(4,6) + "-0" + date.substring(6);
                }
            } else if (date.length() == 8) {
                result = result = date.substring(0,4) + "-" + date.substring(4,6) + "-" + date.substring(6,8);
            } else {
                return null;
            }
        }
        return result;
    }
}