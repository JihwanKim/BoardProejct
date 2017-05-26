package com.example.jihwa.androidbluetoothwithbluecoveprac.protocol;

import java.math.BigInteger;


/**
 * Created by jihwa on 2017-05-25.
 */

public class CreateProtocol {
    private final StartFlag mStartFlag;
    private final EndFlag mEndFlag;
    private final Id mId;
    private final int mLength;
    private byte []mData;

    //  위의 것은 EndFlag를 역할에 맞게 추론해서 자동으로 지정해주고, 아래것은 명시해주는것임.
    public CreateProtocol(StartFlag mStartFlag, Id mId, byte[] mData) {
        this(mStartFlag,EndFlag.getByteUsingStartFlag(mStartFlag),mId,mData);
    }

    public CreateProtocol(StartFlag startFlag, EndFlag endFlag, Id id, byte[] data) {
        this.mStartFlag = startFlag;
        this.mEndFlag = endFlag;
        this.mId = id;
        this.mData = data;
        if(mData != null)
            this.mLength = mData.length;
        else
            this.mLength = 0;
    }

    // 기존에 생성할때의 값들을 (header와 data의 array를 하나로 합쳐서) byte array로 만들어서 리턴함.
    public byte[] toProtocol(){
        byte[] protocol = new byte[5];
        protocol[0] = mStartFlag.getByte();
        protocol[1] = mEndFlag.getByte();
        protocol[2] = mId.getByte();
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

    // srcF와 srcS 의 배열을 append 해서 결과값을 return한다.
    public static byte[] arrayCombine(byte[] srcF,byte[]srcS){
        byte[] bytes = new byte[srcF.length+srcS.length];
        System.arraycopy(srcF,0,bytes,0,srcF.length);
        System.arraycopy(srcS,0,bytes,srcF.length,srcS.length);

        return bytes;
    }
}
