import com.aliyun.odps.udf.UDF;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;

import java.util.*;

public class UDFstringMatching extends UDF {

    /**
     * 向量余弦值计算
     * @param leftArr
     * @param rightArr
     * @return
     */
    public static String cosin(List<Integer> leftArr,List<Integer> rightArr) {

        //向量余弦值分子
        double sum = 0;
        for (int i = 0; i < leftArr.size(); i++) {
            sum += (leftArr.get(i) * rightArr.get(i));
        }
        //向量余弦值分母
        double leftdenominator = 0;
        double rightdenominator = 0;
        for (int i = 0; i < leftArr.size(); i++) {
            leftdenominator += Math.pow(leftArr.get(i), 2);
            rightdenominator += Math.pow(rightArr.get(i), 2);
        }
        double result = sum / (Math.sqrt(leftdenominator) * Math.sqrt(rightdenominator));

        //保留3位小数
        return String.format("%.3f", result);

    }


    // TODO define parameters and return type, e.g:  public String evaluate(String a, String b)

    /**
     * 比较两个字符串的相似度
     * @param a 比较对象
     * @param b 比较对象
     * @return 相似度
     */
    public String evaluate(String a,String b) {
        if (a == null || b == null) {
            return "";
        }
        //HashMap用于存储两个分词的向量
        HashMap<String, Integer> leftHash = new HashMap<>();
        HashMap<String, Integer> rightHash = new HashMap<>();
        Set<String> All = new HashSet<>();

        //删除所有标点符号
        a = a.replaceAll("[\\pP ]", "");
        b = b.replaceAll("[\\pP ]", "");

        //标准切词
        List<Term> left = StandardTokenizer.segment(a);
        List<Term> right = StandardTokenizer.segment(b);

        //将切词后的字段添加到集合中
        List leftList = new ArrayList();
        List rightList = new ArrayList();
        for (Term term : left) {
            leftList.add(term.word);
            All.add(term.word);
        }
        for (Term term : right) {
            rightList.add(term.word);
            All.add(term.word);
        }

        //将left和right的集合转换为hash结构
        for (String s : All) {
            if (leftList.contains(s)) {
                if (!leftHash.containsKey(s)) {
                    leftHash.put(s, 1);
                } else {
                    leftHash.put(s, rightHash.get(s) + 1);
                }
            } else {
                leftHash.put(s, 0);
            }
        }

        for (String s : All) {
            if (rightList.contains(s)) {
                if (!rightHash.containsKey(s)) {
                    rightHash.put(s, 1);
                } else {
                    rightHash.put(s, rightHash.get(s) + 1);
                }
            } else {
                rightHash.put(s, 0);
            }
        }
        //获取left和right数据转换为的向量
        List<Integer> leftArr = new ArrayList();
        Iterator<Integer> leftIter = leftHash.values().iterator();
        while (leftIter.hasNext()) {
            leftArr.add(leftIter.next());
        }

        List<Integer> rightArr = new ArrayList();
        Iterator<Integer> RightIter = rightHash.values().iterator();
        while (RightIter.hasNext()) {
            rightArr.add(RightIter.next());
        }
        return cosin(leftArr, rightArr);

    }

    /**
     * 添加权重词
     * @param a 比较对象
     * @param b 比较对象
     * @param c 忽略词
     * @param d 权重词
     * @param f 权重
     * @return 相似度
     */
    public String evaluate(String a, String b, String c, String d, String f) {

        if (a == null || b == null || c == null || d == null || f == null) {
            return "";
        }
        int weight = Integer.parseInt(f);
        //HashMap用于存储两个分词的向量
        HashMap<String, Integer> leftHash = new HashMap<>();
        HashMap<String, Integer> rightHash = new HashMap<>();
        Set<String> All = new HashSet<>();

        //停用词
        List ignore = new ArrayList();
        for (String s : c.split("#")) {
            ignore.add(s);
        }

        //权重词
        List weighting = new ArrayList();
        for (String s : d.split("#")) {
            weighting.add(s);
        }

        //删除所有标点符号
        a = a.replaceAll("[\\pP ]", "");
        b = b.replaceAll("[\\pP ]", "");

        List<Term> left = StandardTokenizer.segment(a);
        List<Term> right = StandardTokenizer.segment(b);

        List leftList = new ArrayList();
        List rightList = new ArrayList();
        for (Term term : left) {
            leftList.add(term.word);
            All.add(term.word);
        }
        for (Term term : right) {
            rightList.add(term.word);
            All.add(term.word);
        }
        //将left和right的集合转换为hash结构
        for (String s : All) {
            if (leftList.contains(s) && !ignore.contains(s)) {
                if (!leftHash.containsKey(s)) {
                    if (weighting.contains(s)) {
                        leftHash.put(s, weight);
                    } else {
                        leftHash.put(s, 1);
                    }
                } else {
                    if (weighting.contains(s)) {
                        leftHash.put(s, rightHash.get(s) + weight);
                    } else {
                        leftHash.put(s, rightHash.get(s) + 1);
                    }
                }
            } else {
                leftHash.put(s, 0);
            }
        }

        for (String s : All) {
            if (rightList.contains(s) && !ignore.contains(s)) {
                if (!rightHash.containsKey(s)) {
                    if (weighting.contains(s)) {
                        rightHash.put(s, weight);
                    } else {
                        rightHash.put(s, 1);
                    }
                } else {
                    if (weighting.contains(s)) {
                        rightHash.put(s, rightHash.get(s) + weight);
                    } else {
                        rightHash.put(s, rightHash.get(s) + 1);
                    }
                }
            } else {
                rightHash.put(s, 0);
            }
        }
        //获取left和right数据转换为的向量
        List<Integer> leftArr = new ArrayList();
        Iterator<Integer> leftIter = leftHash.values().iterator();
        while (leftIter.hasNext()) {
            leftArr.add(leftIter.next());
        }

        List<Integer> rightArr = new ArrayList();
        Iterator<Integer> RightIter = rightHash.values().iterator();
        while (RightIter.hasNext()) {
            rightArr.add(RightIter.next());
        }

        return cosin(leftArr, rightArr);
    }

}