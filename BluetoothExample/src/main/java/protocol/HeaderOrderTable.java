package protocol;

/**
 * Created by USER on 2017-06-19.
 */
public enum HeaderOrderTable {
    ORDER_01(1),ORDER_02(2),ORDER_03(3),
    //ORDER_04(4),ORDER_05(5),ORDER_06(6),ORDER_07(7),ORDER_08(8),ORDER_09(9),ORDER_10(10),
    //ORDER_11(11),ORDER_12(12),ORDER_13(13),ORDER_14(14),ORDER_15(15),
    ORDER_16(16),ORDER_17(17),ORDER_18(18),ORDER_19(19),
    //ORDER_20(20),
    //ORDER_21(21),ORDER_22(22),ORDER_23(23),ORDER_24(24),ORDER_25(25),ORDER_26(26),ORDER_27(27),ORDER_28(28),ORDER_29(29),ORDER_30(30),
    ORDER_31(31),ORDER_32(32),ORDER_33(33),ORDER_34(34),//ORDER_35(35),ORDER_36(36),ORDER_37(37),ORDER_38,ORDER_39,ORDER_40,
    //ORDER_41,ORDER_42,ORDER_43,ORDER_44,ORDER_45,
    ORDER_46(46),ORDER_47(47),ORDER_48(48),ORDER_49(49),ORDER_50(50),
    //ORDER_51,ORDER_52,ORDER_53,ORDER_54,ORDER_55,ORDER_56,ORDER_57,ORDER_58,ORDER_59,ORDER_60,

    ERROR((byte)0);

    public static HeaderOrderTable makeOrder(final HeaderStartFlag headerStartFlag, final HeaderEndFlag headerEndFlag , final HeaderId headerId){
        HeaderOrderTable result = null;
        if(headerStartFlag == HeaderStartFlag.MODULE_CONTROL){
            if( headerEndFlag == HeaderEndFlag.WRITE){
                if(headerId == HeaderId.CONTROL_START){
                    result = ORDER_01;
                }else if ( headerId == HeaderId.CONTROL_STOP){
                    result = ORDER_02;
                }else if (headerId == HeaderId.CONTROL_RESET){
                    result = ORDER_03;
                }
            }else if (headerEndFlag == HeaderEndFlag.RESPONSE){
            }else if (headerEndFlag== HeaderEndFlag.REQUEST){
            }

        }else if (headerStartFlag == HeaderStartFlag.MODULE_STATUS){
            if(headerEndFlag == HeaderEndFlag.WRITE){
                if(headerId == HeaderId.STATUS_POWER){
                    result = ORDER_16;
                }else if(headerId == HeaderId.STATUS_COIN_BATTERY){
                    result = ORDER_17;
                }else if(headerId == HeaderId.STATUS_SENSOR){
                    result = ORDER_18;
                }else if(headerId == HeaderId.STATUS_MEASURING_TIME){
                    result = ORDER_19;
                }
            }

        }else if (headerStartFlag == HeaderStartFlag.DOSE){
            if(headerEndFlag == HeaderEndFlag.WRITE) {
                if(headerId == HeaderId.DOSE_UPDATE){
                    result = ORDER_31;
                }else if(headerId == HeaderId.DOSE_TOTAL_COUNT){
                    result = ORDER_32;
                }else if(headerId == HeaderId.DOSE_DATA){
                    result = ORDER_33;
                }else if(headerId == HeaderId.DOSE_CHECKSUM){
                    result = ORDER_34;
                }
            }

        }else if (headerStartFlag == HeaderStartFlag.DATA){
            if(headerEndFlag == HeaderEndFlag.WRITE) {
                if (headerId == HeaderId.CONTROL_START) {
                    result = ORDER_46;
                } else if (headerId == HeaderId.DATA_BODY) {
                    result = ORDER_47;
                } else if (headerId == HeaderId.DATA_END) {
                    result = ORDER_48;
                } else if (headerId == HeaderId.DATA_SAVE_YES) {
                    result = ORDER_49;
                } else if (headerId != HeaderId.DATA_SAVE_NO) {
                    result = ORDER_50;
                }
            }
        }

        if(result == null)
            return ERROR;
        return result;
    }

    public static HeaderOrderTable getOrder(byte b) {
        if(b == 0x01)
            return ORDER_01;

        return ERROR;
    }

    private final byte value;
    HeaderOrderTable(int value){
        this.value = (byte)value;
    }


    public byte getByte(){
        return value;
    }
}