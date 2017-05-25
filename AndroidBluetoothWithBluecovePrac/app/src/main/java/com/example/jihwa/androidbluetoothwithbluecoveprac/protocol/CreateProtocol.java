package com.example.jihwa.androidbluetoothwithbluecoveprac.protocol;

import java.math.BigInteger;

import static com.example.jihwa.androidbluetoothwithbluecoveprac.protocol.CreateProtocolHeader.arrayCombine;

/**
 * Created by jihwa on 2017-05-25.
 */

public class CreateProtocol {
    private final StartFlag mStartFlag;
    private final EndFlag mEndFlag;
    private final Id mId;
    private final int mLength;
    private byte []mData;

    public CreateProtocol(StartFlag startFlag, EndFlag endFlag, Id id,byte[] data) {
        this.mStartFlag = startFlag;
        this.mEndFlag = endFlag;
        this.mId = id;
        this.mData = data;
        if(mData != null)
            this.mLength = mData.length;
        else
            this.mLength = 0;
    }

    public byte[] toProtocol(){
        byte[] protocol = new byte[5];
        protocol[0] = StartFlag.getByte(mStartFlag);
        protocol[1] = EndFlag.getByte(mEndFlag);
        protocol[2] = Id.getByte(mId);
        byte[] lengths = new BigInteger(String.valueOf(mLength)).toByteArray();
        if(lengths.length >1){
            protocol[3] = lengths[1];
        }else{
            protocol[3] = 0;
        }
        if(lengths.length>2)
            protocol[4] = lengths[2];
        else
            protocol[4] = lengths[0];
        if(mData!=null)
            protocol = arrayCombine(protocol,mData);
        return protocol;
    }
}
