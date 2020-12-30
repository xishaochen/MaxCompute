import com.aliyun.odps.udf.UDF;


/**
 * 字符串清洗
 */
public class UDFstringClean extends UDF {

    /**
     * 全角对应于ASCII表的可见字符从！开始，偏移值为65281
     */
    private static final char SBC_CHAR_START = 65281;

    /**
     * 全角对应于ASCII表的可见字符到～结束，偏移值为65374
     */
    private static final char SBC_CHAR_END = 65374;

    /**
     * ASCII表中除空格外的可见字符与对应的全角字符的相对偏移
     */
    private static final int CONVERT_STEP = 65248;

    /**
     * 全角空格的值，它没有遵从与ASCII的相对偏移，必须单独处理
     */
    private static final char SBC_SPACE = 12288;

    /**
     * 半角空格的值，在ASCII中为32(Decimal)
     */
    private static final char DBC_SPACE = 32;

    /**
     * 处理逻辑：
     * 1.大小写统一为大写
     * 2.去除空格类字符：包括但不限于 空格 回车 tab。。。
     * 3.全角转换
     */
    public String evaluate(String s) {

        //处理空值
        if (s == null || s.equals("")) {
            return null;
        }

        //将参数转换为数组
        char[] chars = s.toCharArray();

        //创建一个数组，用于存放转换后的值
        StringBuilder builder = new StringBuilder(s.length());

        //遍历数组，将全角字符转换为半角字符
        for (char aChar : chars) {
            if (aChar >= SBC_CHAR_START && aChar <= SBC_CHAR_END) {
                builder.append((char) (aChar - CONVERT_STEP));
            } else if(aChar == SBC_SPACE){ //去除回车、制表符和空格等特殊字符
                builder.append(DBC_SPACE);
            } else {
                builder.append(aChar);
            }
        }
        String result = builder.toString();

        //非datetime数据进行去空格
        String sString="(\\d{2}|\\d{4})(?:-)?([0]{1}\\d{1}|[1]{1}[0-2]{1})(?:-)?([0-2]{1}\\d{1}|[3]{1}[0-1]{1})(?:\\s)?([0-1]{1}\\d{1}|[2]{1}[0-3]{1})(?::)?([0-5]{1}\\d{1})(?::)?([0-5]{1}\\d{1}.0*)";
        if (!result.matches(sString)) {
            result = result.replaceAll(" ","");
        }

        return result.toUpperCase();
    }
}