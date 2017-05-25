package protocol;

/**
 * Created by jihwa on 2017-05-24.
 */


public enum StartFlag {
    MODULE_CONTROL,MODULE_STATUS,DOSE,ERROR, DATA;

    public static byte getByte(StartFlag src)  {
        switch(src){
            case MODULE_CONTROL:
                return 0x01;
            case MODULE_STATUS:
                return 0x02;
            case DATA:
                return 0x03;
            case DOSE:
                return 0x04;
            default:
                return (byte) 0xFF;
        }
    }

    public static StartFlag getStartFlag(byte bt) {
        switch(bt){
            case 0x01:
                return MODULE_CONTROL;
            case 0x02:
                return MODULE_STATUS;
            case 0x03:
                return DATA;
            case 0x04:
                return DOSE;
            default:
                return ERROR;
        }
    }

    public static StartFlag getStartFlag(String str) {
        if (str.toLowerCase().equals("control")) {
            return MODULE_CONTROL;
        }
        if (str.toLowerCase().equals("status")) {
            return MODULE_STATUS;
        }
        if (str.toLowerCase().equals("data")) {
            return DATA;
        }
        if (str.toLowerCase().equals("dose")) {
            return DOSE;
        }
        return ERROR;
    }
}