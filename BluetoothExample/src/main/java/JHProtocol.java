

import java.io.UnsupportedEncodingException;

/**
 * Created by jihwa on 2017-05-19.
 * idx 0~6번째까지는 protocol 정보가 수신됨
 *   의미            약어               byte 값
 * SendMeSsaGe      [SMSG]          918377837193
 * SendFileNaMe     [SFNM]          918370787793
 * SendFileBody     [SFBD]          918370867693
 * SendFileEnD      [SFED]          918370696893
 * 그 이후에는 data 값들이 날라옴.
 */
public class JHProtocol {
    private JHProtocol() {
    }

    public enum Name{
        SMSG, SFNM, SFBD,SFED,NULL;

        public static Name getEnum(byte[] src){
            String str = null;
            try {
                str = new String(src,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if(str == "[SMSG]"){
                return SMSG;
            }
            if(str == "[SFNM]"){
                return SFNM;
            }
            if(str == "[SFBD]"){
                return SFBD;
            }
            if(str == "[SFED]"){
                return SFED;
            }
            return NULL;
        }


        public static byte[] getByte(Name src){
            String str = null;
            switch(src){
                case SMSG:
                    return "[SMSG]".getBytes();
                case SFNM:
                    return "[SFNM]".getBytes();
                case SFBD:
                    return "[SFBD]".getBytes();
                case SFED:
                    return "[SFED]".getBytes();
                default:
                    return "[NULL]".getBytes();
            }
        }

        public static Name getEnum(String src){
            String str = src;
            if(str == "[SMSG]"){
                return SMSG;
            }
            if(str == "[SFNM]"){
                return SFNM;
            }
            if(str == "[SFBD]"){
                return SFBD;
            }
            if(str == "[SFED]"){
                return SFED;
            }
            return NULL;
        }
    }

    public static byte[] arrayCombine(byte[] srcF,byte[]srcS){
        byte[] bytes = new byte[srcF.length+srcS.length];
        System.arraycopy(srcF,0,bytes,0,srcF.length);
        System.arraycopy(srcS,0,bytes,srcF.length,srcS.length);

        return bytes;
    }

    public static byte[] combineProtocolAndBody(Name protocol,byte[] body){
        return arrayCombine(Name.getByte(protocol),body);
    }

    public static byte[] separateGetProtocolReturnByteArray(byte[] src){
        byte [] bytes = new byte[6];
        System.arraycopy(src,0,bytes,0,bytes.length);
        return bytes;
    }

    public static String separateGetProtocolReturnStr(byte[] src) throws UnsupportedEncodingException {
        byte [] bytes = new byte[6];
        System.arraycopy(src,0,bytes,0,bytes.length);
        return new String(bytes,"UTF-8");
    }

    public static byte[] separateGetBodyReturnByteArray(byte[] src){
        byte [] bytes = new byte[src.length-6];
        System.arraycopy(src,6,bytes,0,bytes.length);
        return bytes;
    }

    public static String separateGetBodyReturnStr(byte[] src) throws UnsupportedEncodingException {
        byte [] bytes = new byte[src.length-6];
        System.arraycopy(src,6,bytes,0,bytes.length);
        return new String(bytes,"UTF-8");
    }
}
