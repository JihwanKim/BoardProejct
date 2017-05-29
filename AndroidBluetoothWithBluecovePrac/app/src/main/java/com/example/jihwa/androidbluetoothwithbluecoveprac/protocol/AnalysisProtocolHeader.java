package com.example.jihwa.androidbluetoothwithbluecoveprac.protocol;


import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Created by jihwa on 2017-05-23.
 */

public class AnalysisProtocolHeader {

    private static final int START_FLAG = 1;
    private static final int END_FLAG = 1;
    private static final int ID = 1;
    private static final int LENGTH = 2;
    private static final int DATA_CHECK = 4;
    public static final int HEADER_LENGTH = START_FLAG + END_FLAG + ID + LENGTH +DATA_CHECK;

    byte[] mPacket = null;
    private StartFlag startFlag = null;
    private EndFlag endFlag = null;
    private Id id = null;
    private int crc = 0;

    private int dataLength = 0;

    public AnalysisProtocolHeader(@NotNull byte[] packet) {
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

    public int getCrc(){
        return crc;
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

            crc = java.nio.ByteBuffer.wrap(Arrays.copyOfRange(mPacket,5,9)).getInt();
            Log.d("BluetoothServer","crc = "+crc);
            dataLength = a+b;
            Logging.log("datalength = " + dataLength);
        }
        return startFlag !=null;
    }
}
