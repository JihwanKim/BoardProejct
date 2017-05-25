package protocol;


/**
 * Created by jihwa on 2017-05-24.
 */

public enum EndFlag{
    WRITE,RESPONSE,REQUEST, ERROR;
    public static byte getByte(EndFlag flag){
        switch(flag) {
            case WRITE:
                return 0x01;
            case RESPONSE:
                return 0x02;
            case REQUEST:
                return 0x03;
            default:
                return (byte) 0xFF;
        }
    }

    public static EndFlag getEndFlag(byte bt){
        switch(bt){
            case 0x01:
                return WRITE;
            case 0x02:
                return RESPONSE;
            case 0x03:
                return REQUEST;
            default:
                return ERROR;
        }
    }

    public static byte getByteUsingStartFlag(StartFlag flag){
        switch(flag){
            case MODULE_CONTROL:
                return getByte(WRITE);
            case MODULE_STATUS:
                return getByte(REQUEST);
            case DATA:
                return getByte(WRITE);
            case DOSE:
                return getByte(WRITE);
            case ERROR:
                return getByte(WRITE);
            default:
                return (byte) 0xFF;
        }
    }
}
