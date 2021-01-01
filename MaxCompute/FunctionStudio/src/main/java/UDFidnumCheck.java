import com.aliyun.odps.udf.UDF;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 身份证校验
 * 1.校验身份证号码除了校验位是否均为数值。
 * 2.校验省份位是否为已知的编码。
 * 3.校验出生日期位是否符合日期格式。
 * 4.出生日期逻辑有效性，1984年4月6日国务院发布《中华人民共和国居
 * 民身份证试行条例》，并且开始颁发第一代居民身份证,按寿命120岁验证，年
 * 龄区间是1864年至今。
 * 5.校验位是否正确。
 */
public class UDFidnumCheck extends UDF {
    private final static int NEW_CARD_NUMBER_LENGTH = 18;
    private final static int OLD_CARD_NUMBER_LENGTH = 15;
    private final static int YEAR_BEGIN = 1864;
    private final static char[] VERIFY_CODE = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'}; // 18位身份证中最后一位校验码
    private final static int[] VERIFY_CODE_WEIGHT = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};// 18位身份证中，各个数字的生成校验码时的权值
    private final static int[] PROVINCE_CODE = {11, 12, 13, 14, 15, 21, 22, 23, 31, 32, 33, 34, 35, 36, 37, 41, 42, 43, 44, 45, 46, 50, 51, 52, 53, 54, 61, 62, 63, 64, 65, 71, 81, 82, 91};// 省份编码
    private final static Pattern pattern = Pattern.compile("[0-9]*");

    private final static ThreadLocal<SimpleDateFormat> threadLocal = ThreadLocal.withInitial(() -> {
        return null;// 直接返回null
    });

    public String evaluate(String cardNumbers) {
        //处理非法字符串
        if (cardNumbers == null || cardNumbers.equals("")) {
            return null;
        }

        //清洗cardNumber
        String cardNumber = idNumClean(cardNumbers);

        if (cardNumber.length() == 0 || cardNumber.substring(0,cardNumber.length() - 1).matches("[^0-9]+")) {
            return "18@" + cardNumbers;
        }

        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy");
        int YEAR_END = Integer.parseInt(ft.format(dNow));
        if (NEW_CARD_NUMBER_LENGTH != cardNumber.length() &&
                OLD_CARD_NUMBER_LENGTH != cardNumber.length()) {
            return "18@" + cardNumbers;
        }// 长度不正确
        if (NEW_CARD_NUMBER_LENGTH == cardNumber.length()) {
            if (isNumeric(cardNumber.substring(0, 17))) {
                return "18@" + cardNumbers;
            } // 前17位为数值
            if (calculateVerifyCode(cardNumber) != cardNumber.charAt(17)) {
                return "18@" + cardNumbers; // 校验位不匹配需要打上标记
//                cardNumber = contertToNewCardNumber(cardNumber,NEW_CARD_NUMBER_LENGTH);
            }
            if (isProvince(cardNumber.substring(0, 2))) {
                return "18@" + cardNumbers;
            }// 省份编码在已知集合中
            if (isDate(cardNumber.substring(6, 14))) {
                return "18@" + cardNumbers;
            }// 非法日期转换前后不一致
            int birthdayYear = Integer.parseInt(cardNumber.substring(6, 10));
            int birthdayMonth = Integer.parseInt(cardNumber.substring(10, 12));
            int birthday = Integer.parseInt(cardNumber.substring(12, 14));
            if (birthdayYear < YEAR_BEGIN) {
                return "18@" + cardNumbers;
            }
            if (birthdayYear > YEAR_END) {
                return "18@" + cardNumbers;
            }
            if (birthdayMonth > 12) {
                return "18@" + cardNumbers;
            }
            if (birthday > 31) {
                return "18@" + cardNumbers;
            }
        }
        if (OLD_CARD_NUMBER_LENGTH == cardNumber.length()) {
            if (isNumeric(cardNumber)) {
                return "18@" + cardNumbers;
            } // 前15位为数值
            if (isProvince(cardNumber.substring(0, 2))) {
                return "18@" + cardNumbers;
            }// 省份编码在已知集合中
            if (isDate("19" + cardNumber.substring(6, 12))) {
                return "18@" + cardNumbers;
            }// 非法日期转换前后不一致
            cardNumber = contertToNewCardNumber(cardNumber);
        }
        return cardNumber;
    }

    /**
     * 简单清洗身份证号码
     * @param cardNumber String
     * @return String
     */
    private static String idNumClean(String cardNumber) {
        String result;

        if (!cardNumber.matches("[0-9.xX]+")) {
            result = cardNumber.replaceAll("[^\\d-.-x-X]", "").toUpperCase();
        } else {
            result = cardNumber.replaceAll("[^\\dxX]", "").toUpperCase();
        }

        if (result.length() <= 0 || !result.substring(0,1).matches("[0-9]")) {
            return result.toUpperCase();
        }

        int cardLen = result.replaceAll("\\.","").length();
        int cardLength = result.split("\\.")[0].length();

        if (cardLen == NEW_CARD_NUMBER_LENGTH || cardLen == OLD_CARD_NUMBER_LENGTH) {
            return result.replaceAll("\\.","");
        }

        if (cardLength == NEW_CARD_NUMBER_LENGTH || cardLength == OLD_CARD_NUMBER_LENGTH) {
            return result.split("\\.")[0];
        }
        return result;
    }

    /**
     * 校验码（第十八位数）：
     * <p>
     * 十七位数字本体码加权求和公式 S = Sum(Ai * Wi), i = 0...16 ，先对前17位数字的权求和；
     * Ai:表示第i位置上的身份证号码数字值 Wi:表示第i位置上的加权因子 Wi: 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4
     * 2; 计算模 Y = mod(S, 11) 通过模得到对应的校验码 Y: 0 1 2 3 4 5 6 7 8 9 10 校验码: 1 0 X 9
     * 8 7 6 5 4 3 2
     *
     * @param cardNumber CharSequence
     * @return char
     */
    private static char calculateVerifyCode(CharSequence cardNumber) {
        int sum = 0;
        for (int i = 0; i < NEW_CARD_NUMBER_LENGTH - 1; i++) {
            char ch = cardNumber.charAt(i);
            sum += (ch - '0') * VERIFY_CODE_WEIGHT[i];
        }
        return VERIFY_CODE[sum % 11];
    }

    /**
     * A-把15位身份证号码转换到18位身份证号码 15位身份证号码与18位身份证号码的区别为：
     * 1、15位身份证号码中，"出生年份"字段是2位，转换时需要补入"19"，表示20世纪
     * 2、15位身份证无最后一位校验码。18位身份证中，校验码根据根据前17位生成
     * B-18位身份证号码校验位修复
     *
     * @param cardNumber String
     * @return String
     */
    private static String contertToNewCardNumber(String cardNumber) {
        StringBuilder buf = new StringBuilder(NEW_CARD_NUMBER_LENGTH);
        buf.append(cardNumber, 0, 6);
        buf.append("19");
        buf.append(cardNumber.substring(6));
        buf.append(calculateVerifyCode(buf));
        return buf.toString();
    }

    /**
     * 功能：判断字符串是否为数字
     *
     * @param str String
     * @return Boolean
     */
    private static boolean isNumeric(String str) {
        Matcher isNum = pattern.matcher(str);
        return !isNum.matches();
    }

    /**
     * 功能：判断前两位是否正确
     *
     * @param Province String
     * @return Boolean
     */
    private static boolean isProvince(String Province) {
        int prov = Integer.parseInt(Province);
        for (int j : PROVINCE_CODE) {
            if (j == prov) {
                return false;
            }
        }
        return true;
    }

    public static SimpleDateFormat getDateFormat() {
        SimpleDateFormat df = threadLocal.get();
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
     * @param birthDate String
     * @return boolean
     */
    private static boolean isDate(String birthDate) {
        try {
            SimpleDateFormat sdf = getDateFormat();
            Date cacheBirthDate = sdf.parse(birthDate);
            return !sdf.format(cacheBirthDate).equals(birthDate);// 非法日期转换前后不一致
        } catch (Exception e) {
            return true;
        }
    }
}