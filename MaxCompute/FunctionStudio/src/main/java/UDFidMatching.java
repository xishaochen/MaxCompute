import com.aliyun.odps.udf.UDF;

/**
 * 身份证等数字的相似度匹配
 */
public class UDFidMatching extends UDF {

    public String evaluate(String a,String b) {
    // TODO define parameters and return type, e.g:  public String evaluate(String a, String b)
        char[] left = a.toCharArray();
        char[] right = b.toCharArray();
        double maxLen = Math.max(left.length, right.length);
        int minLen = Math.min(left.length, right.length);

        double sum = 0;
        for (int i = 0; i < minLen; i++) {
            if (left[i] == right[i]) {
                sum += 1;
            }
        }
        double result = sum / maxLen;
        return String.format("%.3f", result);
    }
}