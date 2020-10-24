import com.aliyun.odps.udf.UDF;

public class UDFcharExchange extends UDF {
    // TODO define parameters and return type, e.g:  public String evaluate(String a, String b)
    public String evaluate(String s) {

        //将字段转化为char数组
        char[] chars = s.toCharArray();

        //创建一个数组，用于存放转换后的值
        StringBuilder builder = new StringBuilder(s.length());

        //遍历数组，将全角字符转换为半角字符
        for (char aChar : chars) {
            if (aChar == SBC_SPACE) {
                builder.append(DBC_SPACE);
            } else if (aChar >= SBC_CHAR_START && aChar <= SBC_CHAR_END) {
                builder.append((char) (aChar - CONVERT_STEP));
            } else {
                builder.append(aChar);
            }
        }

        return builder.toString();
    }

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
}