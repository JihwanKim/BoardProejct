package com.example.jihwa.androidbluetoothwithbluecoveprac;

import android.util.Log;

import com.example.jihwa.androidbluetoothwithbluecoveprac.protocol.EndFlag;
import com.example.jihwa.androidbluetoothwithbluecoveprac.protocol.Id;
import com.example.jihwa.androidbluetoothwithbluecoveprac.protocol.StartFlag;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Created by jihwa on 2017-05-23.
 */

public class ProtocolHeader {

    private static final int START_FLAG = 1;
    private static final int END_FLAG = 1;
    private static final int ID = 1;
    private static final int LENGTH = 2;
    public static final int HEADER_LENGTH = START_FLAG + END_FLAG + ID + LENGTH ;

    byte[] mPacket = null;
    private StartFlag startFlag = null;
    private EndFlag endFlag = null;
    private Id id = null;

    private int dataLength = 0;

    public ProtocolHeader(@NotNull byte[] packet) {
        mPacket = packet;
        analysisHeader();

    }

    // 헤더 및 데이터 분석
    // 0xFF : 잘못된 명령으로 전송되었을 경우.
    public StartFlag getStartFlag()  {
        return startFlag;
    }


    public EndFlag getEndFlag()  {
        return endFlag;
    }

    public Id getId()  {
        return id;
    }

    public int getDataLength()  {
        // 왼쪽꺼가 연산이 안됨 .. ? ?
        return dataLength;
    }

    public byte[] getData()  {
        return Arrays.copyOfRange(mPacket,5, dataLength);
    }

    public boolean analysisHeader(){
        if(startFlag == null){
            startFlag = StartFlag.getStartFlag(mPacket[0]);
            endFlag = EndFlag.getEndFlag(mPacket[1]);
            id = Id.getId(mPacket[2]);

            //(((int)mPacket[1]&0xFF)<<8 )+ (int)mPacket[0]&0xFF; ????<< 왜 얘는 안되지 ?
            int a = (mPacket[3]&0xFF)<<8;
            int b = mPacket[4]&0xFF;
            dataLength = a+b;
            Log.d("PACKET part","datalength = " + dataLength);
        }
        return startFlag !=null;
    }


    public static byte[] arrayCombine(byte[] srcF,byte[]srcS){
        byte[] bytes = new byte[srcF.length+srcS.length];
        System.arraycopy(srcF,0,bytes,0,srcF.length);
        System.arraycopy(srcS,0,bytes,srcF.length,srcS.length);

        return bytes;
    }
}
