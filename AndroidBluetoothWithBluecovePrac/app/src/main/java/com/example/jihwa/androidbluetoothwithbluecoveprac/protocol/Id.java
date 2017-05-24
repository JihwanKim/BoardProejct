package com.example.jihwa.androidbluetoothwithbluecoveprac.protocol;

/**
 * Created by jihwa on 2017-05-24.
 */

public enum Id{
    STATUS_POWER, STATUS_COIN_BATTERY, STATUS_SENSOR, CONTROL_TIME, DOSE_CHECKSUM, CONTROL_START, CONTROL_STOP,
    CONTROL_RESET, CONTROL_SLEEP_MODE, STATUS_MEASURING_TIME, DOSE_UPDATE, DOSE_TOTAL_COUNT, DOSE_TOTAL_DATA,
    ERROR, DATA_SAVE_YES, DATA_SAVE_NO, DATA_END, DATA_BODY, DATA_NAME;

    public static Id[] getIdList(){
        return new Id[]{STATUS_POWER, STATUS_COIN_BATTERY, STATUS_SENSOR, CONTROL_TIME, DOSE_CHECKSUM, CONTROL_START, CONTROL_STOP,
                CONTROL_RESET, CONTROL_SLEEP_MODE, STATUS_MEASURING_TIME, DOSE_UPDATE, DOSE_TOTAL_COUNT, DOSE_TOTAL_DATA,
                ERROR, DATA_SAVE_YES, DATA_SAVE_NO, DATA_END, DATA_BODY, DATA_NAME};
    }
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