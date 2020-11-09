import com.aliyun.odps.udf.UDF;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;


public class UDFstringEncry extends UDF {
    Cipher cipher;
    //编码规则，可以任意指定，但是加密与解密的规则必须一致，否则无法解密
    String encodeRules = "default";
    {
        try {
            //1.构造密钥生成器，指定为AES算法,不区分大小写
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            //2.根据ecnodeRules规则初始化密钥生成器
            //生成一个128位的随机源,根据传入的字节数组
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG") ;
            secureRandom.setSeed(encodeRules.getBytes());
            keygen.init(128, secureRandom);
            //3.产生原始对称密钥
            SecretKey original_key = keygen.generateKey();
            //4.获得原始对称密钥的字节数组
            byte[] raw = original_key.getEncoded();
            //5.根据字节数组生成AES密钥
            SecretKey key = new SecretKeySpec(raw, "AES");
            //6.根据指定算法AES自成密码器
            cipher = Cipher.getInstance("AES");
            //7.初始化密码器，第一个参数为加密(Encrypt_mode)或者解密解密(Decrypt_mode)操作，第二个参数为使用的KEY
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String evaluate(String str){
        try {
            //将待加密字符串转换成字节数组
            byte [] byte_encode=str.getBytes("utf-8");
            //进行加密
            byte [] byte_AES=cipher.doFinal(byte_encode);
            return new String(new BASE64Encoder().encode(byte_AES));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        UDFstringEncry encryptionUDF = new UDFstringEncry();
        System.out.println(encryptionUDF.evaluate("MCWABKVKBD"));
        System.out.println(encryptionUDF.evaluate("18920169536"));
        System.out.println(encryptionUDF.evaluate("362204199408305619"));
        System.out.println(encryptionUDF.evaluate("362204199408305618"));
        System.out.println(encryptionUDF.evaluate("362204199508305618"));
        System.out.println(encryptionUDF.evaluate("借用百度出来的最短例程："));
    }

}

