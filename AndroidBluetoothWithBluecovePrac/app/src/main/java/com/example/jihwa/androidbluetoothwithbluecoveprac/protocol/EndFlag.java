package com.example.jihwa.androidbluetoothwithbluecoveprac.protocol;


/**
 * Created by jihwa on 2017-05-24.
 */

public enum EndFlag{
    WRITE {
        @Override
        public byte getByte() {
            return 0x01;
        }
    },RESPONSE {
        @Override
        public byte getByte() {
            return 0x02;
        }
    },REQUEST {
        @Override
        public byte getByte() {
            return 0x03;
        }
    }, ERROR {
        @Override
        public byte getByte() {
            return (byte) 0xFF;
        }
    };

    // enum 값에 해당되는 header에 붙일 byte값을 가져온다.
    public abstract byte getByte();

    // header에 있는 byte값을 가지고 해당 enum 값을 가져온다.
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

    // Start flag를 사용하여, 정해진 EndFlag 값을 반환한다.
    public static EndFlag getByteUsingStartFlag(StartFlag flag){
        switch(flag){
            case MODULE_CONTROL:
                return WRITE;
            case MODULE_STATUS:
                return REQUEST;
            case DATA:
                return WRITE;
            case DOSE:
                return WRITE;
            case ERROR:
                return WRITE;
            default:
                return ERROR;
        }
    }
}
