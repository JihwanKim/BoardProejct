package com.example.jihwa.androidbluetoothwithbluecoveprac;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by jihwa on 2017-05-19.
 * start flag = 0xFA 로 동일
 * test : 0xFA /0x00 0x01 /0x02 /0x01 0xFF/0x00
 * orderName    Start flag = 1byte       Length = 2byte      ID = 1byte      Data bytes = Length       End Flag = 1byte
 *  MODULE_CONTROL        0xFA                                            0x02            0x00 0x00               0xFA[Q]
 *
 *  DATA_SAVE       0xFA                                            0x20            0x00 0x00                   0xFA[R]
 *  DATA_SAVE_Er    0xFA                                            0x20            0x00 0x01                   0xFA[R]
 */
public class JHProtocol {
    private JHProtocol() {
    }
    // 단위 byte
    private static final int START_FLAG = 1;
    private static final int LENGTH = 2;
    private static final int ID = 1;
    private static final int END_FLAG = 1;

    // 헤더생성

    public static byte[] makeHeader(Flag flag,int length, Id id,byte[] data,EndFlag endFlag) throws Exception {
        int byteLength = START_FLAG + LENGTH + ID + END_FLAG + data.length;
        byte[] bytes = new byte[byteLength];
        bytes[0] = Flag.getByte(flag);
        // length
        bytes[1] = (byte) (length & 0xFF00);
        bytes[2] = (byte) (length & 0x00FF);
        //
        bytes[3] = Id.getByte(id);
        System.arraycopy(data,0,bytes,4,data.length);
        bytes[bytes.length-1] = EndFlag.getByte(endFlag);
        return bytes;
    }

    // data가 존재하지 않을 때.
    // exception case : 잘못된 명령
    public static byte[] makeHeader(Flag flag, Id id,EndFlag endFlag) throws Exception {
        int byteLength = START_FLAG + LENGTH + ID + END_FLAG ;
        byte[] bytes = new byte[byteLength];
        bytes[0] = Flag.getByte(flag);
        // length
        bytes[1] = 0x00;
        bytes[2] = 0x01;
        //
        bytes[3] = Id.getByte(id);
        bytes[bytes.length-1] = EndFlag.getByte(endFlag);
        return bytes;
    }

    // 헤더 및 데이터 분석
    // 0xFF : 잘못된 명령으로 전송되었을 경우.
    public static String getStartFlag(byte[] bytes)  {
        return Flag.getOrder(bytes[0]).toString();
    }

    public static int getLength(byte[] bytes)  {
        int a = (bytes[1]&0xFF)<<8;
        int b = bytes[0];
        //(((int)bytes[1]&0xFF)<<8 )+ (int)bytes[0]&0xFF; ????<< 왜 얘는 안되지 ?
        int result = a+b;
        // 왼쪽꺼가 연산이 안됨 .. ? ?
        System.out.println(((bytes[1]&0xFF)<<8 )+ (int)bytes[0]&0xFF);
        return result;
    }

    public static String getId(byte[] bytes)  {
        return Id.getId(bytes[0]).toString();
    }
    public static byte[] getDatas(byte[] bytes)  {
        return Arrays.copyOfRange(bytes,5,bytes.length-2);
    }
    public static String getEndFlag(byte[] bytes)  {
        return EndFlag.getEndFlag(bytes[bytes.length-1]).toString();
    }

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
    }
    public enum Id{
        POWER_ON,  COIN_BATTERY, SENSOR, TIME, DOSE_CHECKSUM, START, STOP, RESET, SLEEP_MODE, STATUS_TIME, DOSE_UPDATE, DOSE_TOTAL_COUNT, DOSE_TOTAL_DATA, ERROR, DATA_SAVE_YES, DATA_SAVE_NO;
        public static byte getByte(Id id){
            switch(id){
                case START:
                    return 0x01;
                case STOP:
                    return 0x02;
                case RESET:
                    return 0x03;
                case SLEEP_MODE:
                    return 0x04;
                case TIME:
                    return 0x05;

                case POWER_ON:
                    return 0x06;
                case COIN_BATTERY:
                    return 0x07;
                case SENSOR:
                    return 0x08;
                case STATUS_TIME:
                    return 0x09;

                case DOSE_UPDATE:
                    return 0x0B;
                case DOSE_TOTAL_COUNT:
                    return 0x0C;
                case DOSE_TOTAL_DATA:
                    return 0x0D;
                case DOSE_CHECKSUM:
                    return 0x0E;

                case DATA_SAVE_YES:
                    return 0x10;
                case DATA_SAVE_NO:
                    return 0x11;

                case ERROR:
                    return 0x0F;

                default:
                    return (byte) 0xFF;
            }
        }
        public static Id getId(byte id){
            switch(id){
                case 0x01:
                    return START;
                case 0x02:
                    return STOP;
                case 0x03:
                    return RESET;
                case 0x04:
                    return SLEEP_MODE;
                case 0x05:
                    return TIME;

                case 0x06:
                    return POWER_ON;
                case 0x07:
                    return COIN_BATTERY;
                case 0x08:
                    return SENSOR;
                case 0x09:
                    return STATUS_TIME;
                case 0x0A:
                    return ERROR;

                case 0x0B:
                    return DOSE_UPDATE;
                case 0x0C:
                    return DOSE_TOTAL_COUNT;
                case 0x0D:
                    return DOSE_TOTAL_DATA;
                case 0x0E:
                    return DOSE_CHECKSUM;
                case 0x0F:
                    return ERROR;

                case 0x10:
                    return DATA_SAVE_YES;
                case 0x11:
                    return DATA_SAVE_NO;
                case 0x12:
                    return ERROR;
                case 0x13:
                    return ERROR;
                case 0x14:
                    return ERROR;
                case 0x15:
                    return ERROR;
                default:
                    return ERROR;
            }
        }
    }

    public enum Flag {
        SMSG, SFNM, SFBD,SFED,NULL
        , MODULE_CONTROL,MODULE_STATUS,DOSE,ERROR,SAVE;

        public static Flag getEnum(byte[] src){
            String str = null;
            try {
                str = new String(src,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.d("JHProtocol","unsupported Encoding ." + e.getMessage());
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


        public static byte[] getBytes(Flag src){
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

        public static byte getByte(Flag src)  {
            switch(src){
                case MODULE_CONTROL:
                    return 0x01;
                case MODULE_STATUS:
                    return 0x02;
                case SAVE:
                    return 0x03;
                case DOSE:
                    return 0x04;
                default:
                    return (byte) 0xFF;
            }
        }
        public static Flag getOrder(byte bt) {
            switch(bt){
                case 0x01:
                    return MODULE_CONTROL;
                case 0x02:
                    return MODULE_STATUS;
                case 0x03:
                    return SAVE;
                case 0x04:
                    return DOSE;
                default:
                    return ERROR;
            }
        }


        public static String getString(Flag src){
            switch(src){
                case SMSG:
                    return "[SMSG]";
                case SFNM:
                    return "[SFNM]";
                case SFBD:
                    return "[SFBD]";
                case SFED:
                    return "[SFED]";
                default:
                    return "[NULL]";
            }
        }

        public static Flag getEnum(String src){
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

    public static byte[] combineProtocolAndBody(Flag protocol, byte[] body){
        return arrayCombine(Flag.getBytes(protocol),body);
    }


    public static boolean isEndReceiveFile(byte[] src){
        byte[] bytes = new byte[6];
        System.arraycopy(src,src.length-7,bytes,0,bytes.length);
        if (Flag.getEnum(bytes) == Flag.SFED) {
            return true;
        }
        return false;
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

}
