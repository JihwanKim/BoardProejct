package protocol;

/**
 * Created by jihwa on 2017-05-24.
 */

public enum HeaderId {
    CONTROL_START {
        @Override
        public byte getByte() {
            return 0x01;
        }
    }, CONTROL_STOP {
        @Override
        public byte getByte() {
            return 0x02;
        }
    }, CONTROL_RESET {
        @Override
        public byte getByte() {
            return 0x03;
        }
    }, CONTROL_SLEEP_MODE {
        @Override
        public byte getByte() {
            return 0x04;
        }
    }, CONTROL_TIME {
        @Override
        public byte getByte() {
            return 0x05;
        }
    }

    , STATUS_POWER {
        @Override
        public byte getByte() {
            return 0x06;
        }
    }, STATUS_COIN_BATTERY {
        @Override
        public byte getByte() {
            return 0x07;
        }
    }, STATUS_SENSOR {
        @Override
        public byte getByte() {
            return 0x08;
        }
    }, STATUS_MEASURING_TIME {
        @Override
        public byte getByte() {
            return 0x09;
        }
    }

    , DOSE_UPDATE {
        @Override
        public byte getByte() {
            return 0x0B;
        }
    }, DOSE_TOTAL_COUNT {
        @Override
        public byte getByte() {
            return 0x0C;
        }
    }, DOSE_DATA {
        @Override
        public byte getByte() {
            return 0x0D;
        }
    }, DOSE_CHECKSUM {
        @Override
        public byte getByte() {
            return 0x0E;
        }
    }

    , DATA_START {
        @Override
        public byte getByte() {
            return 0x11;
        }
    }, DATA_BODY {
        @Override
        public byte getByte() {
            return 0x12;
        }
    }, DATA_END {
        @Override
        public byte getByte() {
            return 0x13;
        }
    }, DATA_SAVE_YES {
        @Override
        public byte getByte() {
            return 0x14;
        }
    }, DATA_SAVE_NO {
        @Override
        public byte getByte() {
            return 0x15;
        }
    }

    , ERROR {
        @Override
        public byte getByte() {
            return (byte) 0xFF;
        }
    };

    public static HeaderId[] getIdList(){
        return new HeaderId[]{STATUS_POWER, STATUS_COIN_BATTERY, STATUS_SENSOR, CONTROL_TIME, DOSE_CHECKSUM, CONTROL_START, CONTROL_STOP,
                CONTROL_RESET, CONTROL_SLEEP_MODE, STATUS_MEASURING_TIME, DOSE_UPDATE, DOSE_TOTAL_COUNT, DOSE_DATA,
                ERROR, DATA_SAVE_YES, DATA_SAVE_NO, DATA_END, DATA_BODY, DATA_START};
    }

    // enum 값에 해당되는 header에 붙일 byte값을 가져온다.
    public abstract byte getByte();

    // header에 있는 byte값을 가지고 해당 enum 값을 가져온다.
    public static HeaderId getId(byte id){
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
                return DOSE_DATA;
            case 0x0E:
                return DOSE_CHECKSUM;
            case 0x0F:
                return ERROR;

            case 0x11:
                return DATA_START;
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


    // String값을 가지고 해당 enum 값을 가져온다.
    public static HeaderId getId(String str){
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
            return DOSE_DATA;
        if(str.toLowerCase().equals("checksum"))
            return DOSE_CHECKSUM;
        if(str.toLowerCase().equals("error"))
            return ERROR;

        if(str.toLowerCase().equals("name"))
            return DATA_START;
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