package com.example.jihwa.androidbluetoothwithbluecoveprac;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * Created by jihwa on 2017-05-19.
 * start flag = 0xFA 로 동일
 * test : 0xFA /0x00 0x01 /0x02 /0x01 0xFF/0x00
 * orderName    Start flag = 1byte       Length = 2byte      ID = 1byte      Data bytes = Length       End StartFlag = 1byte
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
    private static final int END_FLAG = 1;
    private static final int ID = 1;
    private static final int LENGTH = 2;
    public static final int HEADER_LENGTH = START_FLAG + END_FLAG + ID + LENGTH ;

    // 헤더생성
    // exception case : 잘못된 명령
    // 1 1 1 2
    public static byte[] makeHeader(StartFlag startFlag, Id id) {
        byte[] bytes = new byte[HEADER_LENGTH];
        bytes[0] = StartFlag.getByte(startFlag);
        bytes[1] = EndFlag.getByteUsingStartFlag(startFlag);
        bytes[2] = Id.getByte(id);
        //
        return bytes;
    }

    @SuppressWarnings("Since15")
    public static byte[] getHeader(byte[] bytes){
        return Arrays.copyOfRange(bytes,0, HEADER_LENGTH);
    }

    public static byte[] makePacket(byte[] header,byte[] data){
        // dataLength
        if(data != null && data.length > 0) {
            byte[] dataLength = BigInteger.valueOf(data.length).toByteArray();
            System.out.println("datalength = "+dataLength.length + "  real length = " + data.length );
            for(int i = 0 ; i < dataLength.length ; i++){
                System.out.println(i+ " + " + dataLength[i]);
            }
            if(dataLength.length != 1)
                header[3] = dataLength[1];
            if(dataLength[0] == 0 && dataLength.length > 2)
                header[4] = dataLength[2];
            else
                header[4] = dataLength[0];
            return arrayCombine(header, data);
        }
        header[3] = 0x00;
        header[4] = 0x00;
        return header;
    }

    // 헤더 및 데이터 분석
    // 0xFF : 잘못된 명령으로 전송되었을 경우.
    public static StartFlag getStartFlag(byte[] bytes)  {
        return StartFlag.getStartFlag(bytes[0]);
    }


    public static EndFlag getEndFlag(byte[] bytes)  {
        return EndFlag.getEndFlag(bytes[1]);
    }

    public static Id getId(byte[] bytes)  {
        return Id.getId(bytes[2]);
    }

    public static int getLength(byte[] bytes)  {
        int a = (bytes[3]&0xFF)<<8;
        int b = bytes[4];
        //(((int)bytes[1]&0xFF)<<8 )+ (int)bytes[0]&0xFF; ????<< 왜 얘는 안되지 ?
        int result = a+b;
        // 왼쪽꺼가 연산이 안됨 .. ? ?
        System.out.println(((bytes[1]&0xFF)<<8 )+ (int)bytes[0]&0xFF);
        return result;
    }

    @SuppressWarnings("Since15")
    public static byte[] getData(byte[] bytes,int length)  {
        return Arrays.copyOfRange(bytes,5,length);
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

    public enum Id{
        STATUS_POWER, STATUS_COIN_BATTERY, STATUS_SENSOR, CONTROL_TIME, DOSE_CHECKSUM, CONTROL_START, CONTROL_STOP, CONTROL_RESET, CONTROL_SLEEP_MODE,
        STATUS_MEASURING_TIME, DOSE_UPDATE, DOSE_TOTAL_COUNT, DOSE_TOTAL_DATA, ERROR, DATA_SAVE_YES, DATA_SAVE_NO,
        DATA_END, DATA_BODY, DATA_NAME;
        public static byte getByte(Id id){
            switch(id){
                case CONTROL_START:
                    return 0x01;
                case CONTROL_STOP:
                    return 0x02;
                case CONTROL_RESET:
                    return 0x03;
                case CONTROL_SLEEP_MODE:
                    return 0x04;
                case CONTROL_TIME:
                    return 0x05;

                case STATUS_POWER:
                    return 0x06;
                case STATUS_COIN_BATTERY:
                    return 0x07;
                case STATUS_SENSOR:
                    return 0x08;
                case STATUS_MEASURING_TIME:
                    return 0x09;

                case DOSE_UPDATE:
                    return 0x0B;
                case DOSE_TOTAL_COUNT:
                    return 0x0C;
                case DOSE_TOTAL_DATA:
                    return 0x0D;
                case DOSE_CHECKSUM:
                    return 0x0E;

                case DATA_NAME:
                    return 0x11;
                case DATA_BODY:
                    return 0x12;
                case DATA_END:
                    return 0x13;
                case DATA_SAVE_YES:
                    return 0x14;
                case DATA_SAVE_NO:
                    return 0x15;

                case ERROR:
                    return 0x0F;

                default:
                    return (byte) 0xFF;
            }
        }

        public static Id getId(byte id){
            switch(id){
                case 0x01:
                    return CONTROL_START;
                case 0x02:
                    return CONTROL_STOP;
                case 0x03:
                    return CONTROL_RESET;
                case 0x04:
                    return CONTROL_SLEEP_MODE;
                case 0x05:
                    return CONTROL_TIME;

                case 0x06:
                    return STATUS_POWER;
                case 0x07:
                    return STATUS_COIN_BATTERY;
                case 0x08:
                    return STATUS_SENSOR;
                case 0x09:
                    return STATUS_MEASURING_TIME;
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

                case 0x11:
                    return DATA_NAME;
                case 0x12:
                    return DATA_BODY;
                case 0x13:
                    return DATA_END;
                case 0x14:
                    return DATA_SAVE_YES;
                case 0x15:
                    return DATA_SAVE_NO;
                default:
                    return ERROR;
            }
        }


        public static Id getId(String str){
            if(str.toLowerCase().equals("start"))
                return CONTROL_START;
            if(str.toLowerCase().equals("stop"))
                return CONTROL_STOP;
            if(str.toLowerCase().equals("reset"))
                return CONTROL_RESET;
            if(str.toLowerCase().equals("sleep"))
                return CONTROL_SLEEP_MODE;
            if(str.toLowerCase().equals("measuring_time"))
                return STATUS_MEASURING_TIME;

            if(str.toLowerCase().equals("power"))
                return STATUS_POWER;
            if(str.toLowerCase().equals("battery"))
                return STATUS_COIN_BATTERY;
            if(str.toLowerCase().equals("sensor"))
                return STATUS_SENSOR;
            if(str.toLowerCase().equals("measuring_time"))
                return STATUS_MEASURING_TIME;
            if(str.toLowerCase().equals("error"))
                return ERROR;

            if(str.toLowerCase().equals("update"))
                return DOSE_UPDATE;
            if(str.toLowerCase().equals("total_count"))
                return DOSE_TOTAL_COUNT;
            if(str.toLowerCase().equals("total_data"))
                return DOSE_TOTAL_DATA;
            if(str.toLowerCase().equals("checksum"))
                return DOSE_CHECKSUM;
            if(str.toLowerCase().equals("error"))
                return ERROR;

            if(str.toLowerCase().equals("name"))
                return DATA_NAME;
            if(str.toLowerCase().equals("body"))
                return DATA_BODY;
            if(str.toLowerCase().equals("end"))
                return DATA_END;
            if(str.toLowerCase().equals("save_yes"))
                return DATA_SAVE_YES;
            if(str.toLowerCase().equals("save_no"))
                return DATA_SAVE_NO;
            if(str.toLowerCase().equals("error"))
                return ERROR;
            if(str.toLowerCase().equals("error"))
                return ERROR;

            return ERROR;
        }
    }

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

    public static byte[] arrayCombine(byte[] srcF,byte[]srcS){
        byte[] bytes = new byte[srcF.length+srcS.length];
        System.arraycopy(srcF,0,bytes,0,srcF.length);
        System.arraycopy(srcS,0,bytes,srcF.length,srcS.length);

        return bytes;
    }

    // 명령어 . Start > ?
    // 명령어 .
    // control start
    // status power
    // data save > 저장할 데이터를 보내라.
    //
    public static byte[] convertToPacket(String str){
        byte packet[]= null;
        byte header[] = null;
        byte data[] = null;
        String[] order = str.split(" ");
        String thirdOrder = null;

        StartFlag flag = StartFlag.getStartFlag(order[0]);
        Id id = Id.getId(order[1]);
        if(order.length>2){
            thirdOrder = order[2];
            int time = 0;
            try{
                time = Integer.parseInt(thirdOrder);
            }catch(Exception e){

            }

            if(time ==0){
                File file = new File(thirdOrder);
                try {
                    FileInputStream oInputStream = new FileInputStream(file);
                    int nCount = oInputStream.available();
                    if (nCount > 0) {
                        data = new byte[nCount];
                        oInputStream.read(data);
                    }

                    if (oInputStream != null) {
                        oInputStream.close();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                data = BigInteger.valueOf(time).toByteArray();
            }
        }
        header = makeHeader(flag, id);

        packet = makePacket(header, data);

        return packet;
    }
}
