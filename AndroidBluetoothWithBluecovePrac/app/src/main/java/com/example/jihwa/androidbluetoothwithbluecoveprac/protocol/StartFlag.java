package com.example.jihwa.androidbluetoothwithbluecoveprac.protocol;

/**
 * Created by jihwa on 2017-05-24.
 */


public enum StartFlag {
    MODULE_CONTROL {
        @Override
        public byte getByte() {
            return 0x01;
        }
    },MODULE_STATUS {
        @Override
        public byte getByte() {
            return 0x02;
        }
    }, DATA {
        @Override
        public byte getByte() {
            return 0x03;
        }
    },DOSE {
        @Override
        public byte getByte() {
            return 0x04;
        }
    },ERROR {
        @Override
        public byte getByte() {
            return (byte) 0xFF;
        }
    };

    // enum 값에 해당되는 header에 붙일 byte값을 가져온다.
    public abstract byte getByte();

    // header에 있는 byte값을 가지고 해당 enum 값을 가져온다.
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