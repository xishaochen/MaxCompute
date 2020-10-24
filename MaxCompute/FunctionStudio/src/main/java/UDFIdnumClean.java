import com.aliyun.odps.udf.UDF;

/***
 * 身份证号码构成：6位地址编码+8位生日+3位顺序码+1位校验码
 * 验证15位，18位证件号码是否有效；15位号码将直接转为18位；
 * 校验身份证号码除了校验位是否为数值，校验省份、出生日期
 * 校验位不正确的会被正确的替代
 * 出生日期逻辑有效性，即是否1900年前出生，是否当前日期后出生未校验
 * 1984年4月6日国务院发布《中华人民共和国居民身份证试行条例》，并且开始颁发第一代居民身份证,按寿命120岁验证，年龄区间是1864年至今。
 * 15位证件号码转换未考虑1900年前出生的人
 ***/
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.SimpleDateFormat;
import java.util.Date;


public class UDFIdnumClean extends UDF {
    private final static int NEW_CARD_NUMBER_LENGTH = 18;
    private final static int OLD_CARD_NUMBER_LENGTH = 15;
    private final static int YEAR_BEGIN = 1864;
    private final static char[] VERIFY_CODE = { '1', '0', 'X', '9', '8', '7','6', '5', '4', '3', '2' }; // 18位身份证中最后一位校验码
    private final static int[] VERIFY_CODE_WEIGHT = { 7, 9, 10, 5, 8, 4, 2, 1,6, 3, 7, 9, 10, 5, 8, 4, 2 };// 18位身份证中，各个数字的生成校验码时的权值
    private final static int[] PROVINCE_CODE = { 11, 12, 13, 14, 15, 21, 22,23, 31, 32, 33, 34, 35, 36, 37, 41, 42, 43, 44, 45, 46, 50, 51, 52,53, 54, 61, 62, 63, 64, 65, 71, 81, 82, 91 };// 省份编码
    private final static Pattern pattern = Pattern.compile("[0-9]*");
    private final static ThreadLocal<SimpleDateFormat> threadLocal = new ThreadLocal<SimpleDateFormat>() {
        protected SimpleDateFormat initialValue() {
            return null;// 直接返回null
        }
    };

    public String evaluate(String cardNumber) {
        if (cardNumber == null) {return null;}// 为NULL
        Date dNow = new Date( );
        SimpleDateFormat ft = new SimpleDateFormat ("yyyy");
        int YEAR_END = Integer.parseInt(ft.format(dNow));
        cardNumber = cardNumber.trim();
        if (cardNumber.length() == 0) {return null;}// 空字符串
        if (NEW_CARD_NUMBER_LENGTH != cardNumber.length() &
                OLD_CARD_NUMBER_LENGTH != cardNumber.length()) {return null;}// 长度不正确
        if (NEW_CARD_NUMBER_LENGTH == cardNumber.length()) {
            if (isNumeric(cardNumber.substring(0, 17)) == false) {return null;} // 前17位为数值
            if (UDFIdnumClean.calculateVerifyCode(cardNumber) != cardNumber.charAt(17)) {
                cardNumber = contertToNewCardNumber(cardNumber,NEW_CARD_NUMBER_LENGTH);
            }// 校验位修正
            if (isProvince(cardNumber.substring(0, 2)) == false) {return null;}// 省份编码在已知集合中
            if (isDate(cardNumber.substring(6, 14)) == false) {return null;}// 非法日期转换前后不一致
            int birthdayYear = Integer.parseInt(cardNumber.substring(6, 10));
            if (birthdayYear < YEAR_BEGIN) {return null;}// 为NULL
            if (birthdayYear > YEAR_END) {return null;}// 为NULL
        }
        if (OLD_CARD_NUMBER_LENGTH == cardNumber.length()) {
            if (isNumeric(cardNumber) == false) {return null;} // 前15位为数值
            if (isProvince(cardNumber.substring(0, 2)) == false) {return null;}// 省份编码在已知集合中
            if (isDate("19" + cardNumber.substring(6, 12)) == false) {return null;}// 非法日期转换前后不一致
            cardNumber = contertToNewCardNumber(cardNumber,OLD_CARD_NUMBER_LENGTH);
        }
        return cardNumber;
    }
    /**
     * 校验码（第十八位数）：
     *
     * 十七位数字本体码加权求和公式 S = Sum(Ai * Wi), i = 0...16 ，先对前17位数字的权求和；
     * Ai:表示第i位置上的身份证号码数字值 Wi:表示第i位置上的加权因子 Wi: 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4
     * 2; 计算模 Y = mod(S, 11) 通过模得到对应的校验码 Y: 0 1 2 3 4 5 6 7 8 9 10 校验码: 1 0 X 9
     * 8 7 6 5 4 3 2
     *
     * @param cardNumber
     * @return
     */
    private static char calculateVerifyCode(CharSequence cardNumber) {
        int sum = 0;
        for (int i = 0; i < NEW_CARD_NUMBER_LENGTH - 1; i++) {
            char ch = cardNumber.charAt(i);
            sum += ((int) (ch - '0')) * VERIFY_CODE_WEIGHT[i];
        }
        return VERIFY_CODE[sum % 11];
    }

    /**
     * A-把15位身份证号码转换到18位身份证号码 15位身份证号码与18位身份证号码的区别为：
     * 1、15位身份证号码中，"出生年份"字段是2位，转换时需要补入"19"，表示20世纪
     * 2、15位身份证无最后一位校验码。18位身份证中，校验码根据根据前17位生成
     * B-18位身份证号码校验位修复
     * @param cardNumber
     * @return
     */
    private static String contertToNewCardNumber(String cardNumber,int cardNumberLength) {
        StringBuilder buf = new StringBuilder(NEW_CARD_NUMBER_LENGTH);
        if       (cardNumberLength == NEW_CARD_NUMBER_LENGTH){
            buf.append(cardNumber.substring(0, 17));
            buf.append(UDFIdnumClean.calculateVerifyCode(buf));
        }else if (cardNumberLength == OLD_CARD_NUMBER_LENGTH){
            buf.append(cardNumber.substring(0, 6));
            buf.append("19");
            buf.append(cardNumber.substring(6));
            buf.append(UDFIdnumClean.calculateVerifyCode(buf));
        }
        return buf.toString();
    }

    /**
     * 功能：判断字符串是否为数字
     *
     * @param str
     * @return
     */
    private static boolean isNumeric(String str) {
        Matcher isNum = pattern.matcher(str);
        if (isNum.matches()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 功能：判断前两位是否正确
     *
     * @param Province
     * @return
     */
    private static boolean isProvince(String Province) {
        int prov = Integer.parseInt(Province);
        for (int i = 0; i < PROVINCE_CODE.length; i++) {
            if (PROVINCE_CODE[i] == prov) {
                return true;
            }
        }
        return false;
    }

    public static SimpleDateFormat getDateFormat() {
        SimpleDateFormat df = (SimpleDateFormat) threadLocal.get();
        if (df == null) {
            df = new SimpleDateFormat("yyyyMMdd");
            df.setLenient(false);
            threadLocal.set(df);
        }
        return df;
    }

    /**
     * 功能：判断出生日期字段是否为日期
     *
     * @param birthDate
     * @return
     */
    private static boolean isDate(String birthDate) {
        try {
            SimpleDateFormat sdf = getDateFormat();
            Date cacheBirthDate = sdf.parse(birthDate);
            if (sdf.format(cacheBirthDate).equals(birthDate) == false) {return false;}// 非法日期转换前后不一致
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}