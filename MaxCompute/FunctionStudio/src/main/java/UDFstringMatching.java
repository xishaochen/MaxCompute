import com.aliyun.odps.udf.UDF;
import com.hankcs.hanlp.dependency.nnparser.util.math;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;

import java.util.*;


public class UDFstringMatching extends UDF {
    // TODO define parameters and return type, e.g:  public String evaluate(String a, String b)
    public Double evaluate(String a,String b) {
        //HashMap用于存储两个分词的向量
        HashMap<String, Integer> leftHash = new HashMap<>();
        HashMap<String, Integer> rightHash = new HashMap<>();
        Set<String> All = new HashSet<>();


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
            if (leftList.contains(s)) {
                if (!leftHash.containsKey(s)) {
                    leftHash.put(s,1);
                } else {
                    leftHash.put(s,leftHash.get(s) + 1);
                }
            } else {
                leftHash.put(s,0);
            }
        }

        for (String s : All) {
            if (rightList.contains(s)) {
                if (!rightHash.containsKey(s)) {
                    rightHash.put(s,1);
                } else {
                    rightHash.put(s,rightHash.get(s) + 1);
                }
            } else {
                rightHash.put(s,0);
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

        //向量余弦值分子
        double sum = 0;
        for (int i = 0; i < leftArr.size(); i++) {
            sum += (leftArr.get(i) * rightArr.get(i));
        }
        System.out.println(sum);
        //向量余弦值分母
        double leftdenominator = 0;
        double rightdenominator = 0;
        for (int i = 0; i < leftArr.size(); i++) {
            leftdenominator += Math.pow(leftArr.get(i),2);
            rightdenominator += Math.pow(rightArr.get(i),2);
        }

        return sum / (Math.sqrt(leftdenominator) * Math.sqrt(rightdenominator));

    }

}