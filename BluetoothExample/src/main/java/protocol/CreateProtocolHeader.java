package com.example.jihwa.androidbluetoothwithbluecoveprac.protocol;

import java.math.BigInteger;

/**
 * Created by jihwa on 2017-05-25.
 */

public class CreateProtocolHeader {
    private final StartFlag mStartFlag;
    private final EndFlag mEndFlag;
    private final Id mId;
    private final int mLength;

    public CreateProtocolHeader(StartFlag startFlag, EndFlag endFlag, Id id, int length) {
        this.mStartFlag = startFlag;
        this.mEndFlag = endFlag;
        this.mId = id;
        this.mLength = length;
    }

    public byte[] toHeader(){
        byte[] header = new byte[5];
        header[0] = StartFlag.getByte(mStartFlag);
        header[1] = EndFlag.getByte(mEndFlag);
        header[2] = Id.getByte(mId);
        byte[] lengths = new BigInteger(String.valueOf(mLength)).toByteArray();
        if(lengths.length >1){
            header[3] = lengths[1];
        }else{
            header[3] = 0;
        }
        if(lengths.length>2)
            header[4] = lengths[2];
        else
            header[4] = lengths[0];

        return header;
    }
}
