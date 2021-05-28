import com.aliyun.odps.udf.UDF;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UDFRegexpExtractAll extends UDF {
    // TODO define parameters and return type, e.g:  public String evaluate(String a, String b)
    public List<String> evaluate(String source,String regex,Integer...indexs) throws Exception {
        if ("".equals(source)||source==null){
            List<String> collection=new ArrayList<>();
            collection.add("");
            return collection;
        }else {
            return findAll(regex, source, indexs);
        }
    }



    public static List<String> findAll(String regex, CharSequence content,Integer... indexs) throws Exception {
        List<String> collection = new ArrayList<>();
        List<String> tempCollection=new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        //indexs.length<1,说明未指定取第几位匹配项，返回全部匹配项List
        if (indexs.length<1){
            while(matcher.find()) {
                collection.add(matcher.group());
            }
        }else {
            while(matcher.find()) {
                tempCollection.add(matcher.group());
            }
            int tempIndex=indexs[0];
            int index;
            if(tempIndex>0){
                //索引位置从0开始，为了便于理解，传参从1开始
                index=tempIndex-1;
            }else if (tempIndex<0){
                //index为负数时，从结尾取匹配项
                index=tempCollection.size()+tempIndex;
            }else {
                //index不能为0
                throw new Exception("'index' parameter is not allowed to be 0");
            }
            //index超过匹配项数，返回""
            if (tempCollection.size()-1<index||index<0){
                collection.add("");
            }else {
                collection.add(tempCollection.get(index));
            }
        }
        return collection;
    }
}