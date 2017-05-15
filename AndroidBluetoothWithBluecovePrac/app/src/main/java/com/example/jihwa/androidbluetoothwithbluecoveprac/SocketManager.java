package com.example.jihwa.androidbluetoothwithbluecoveprac;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;


/**
 * Created by jihwa on 2017-05-15.
 */

public class SocketManager {
    private static final String TAG = "BluetoothServer";
    private BluetoothSocket mBluetoothSocket = null;
    private InputStream mInputStream = null;
    private OutputStream mOutputStream = null;
    private ArrayBlockingQueue<String> mMsg = new ArrayBlockingQueue<>(20);


    private boolean isRunning = false;

    public SocketManager(@NotNull BluetoothSocket socket){
        mBluetoothSocket = socket;
        isRunning = true;
        try {
            mInputStream = mBluetoothSocket.getInputStream();
            mOutputStream = mBluetoothSocket.getOutputStream();
        } catch (IOException e) {
            Log.d(TAG,"socket closed " + e.getMessage());
        }

        Thread receiveThread = new Thread(){
            @Override
            public void run() {
                byte[] readBuffer = new byte[1024];
                int readBufferPosition = 0;
                while(true){
                    try{
                        if(!mBluetoothSocket.isConnected()) {
                            isRunning = false;
                            return;
                        }
                        // 근데 왜또되냐 진짜 미치겠네.
                        // 읽을 수 있는 bytes 를 확인함
                        // 아래는 주석 두줄은 구글번역기
                        // 블록하지 않고이 입력 Stream로부터 읽어 들일 수가있는 (또는 스킵 할 수있다)
                        // 바이트 수의 추정치. 입력 Stream의 마지막에 이르렀을 때의 바이트 수.
                        int bytesAvailable = mInputStream.available();
                        // 만약, 대기중인게 있을 경우,
                        if(bytesAvailable>0){
                            // packet으로 온 bytes를 저장할 변수를 선언함
                            byte[] packetBytes = new byte[bytesAvailable];
                            //read from the inputStream
                            // mInputStream에서 읽어온 bytes를 packetBytes에 저장함.
                            mInputStream.read(packetBytes);

                            // 추정된 바이트의 길이보다 작을동안 실행.
                            for(int i = 0 ; i<bytesAvailable; i++){
                                // b를 packetBytes 의 i번째 byte로 초기화시킴.
                                byte b = packetBytes[i];
                                // 만약, 엔터 이스케이프 시퀸스가 발견될 경우,
                                // 읽어들인 readBufferPosition의 크기를 지닌 encodedBytes를 선언하는데,
                                // 해당 변수는 결과적으로 readBuffer에 저장되어있는 모든 배열을 카피해감.
                                // TODO : check . why arraycopy readBuffer to encodedBytes
                                if(b == '\n'){
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer,0,encodedBytes,0,encodedBytes.length);
                                    // encodedBytes에 저장되어있는 값을 UTF-8 인코딩으로 recvMessage에
                                    // 저장함.
                                    String recvMessage = new String(encodedBytes,"UTF-8");

                                    // readBufferPosition에 0을 대입함.
                                    readBufferPosition = 0;

                                    Log.d(TAG,"ReceiveMsg = "+recvMessage);
                                    mMsg.add(recvMessage);
                                }else{
                                    // 그렇지 않으면,
                                    // readBuffer에 readBufferPostion의 위치에 b를 저장함.
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException e) {
                        Log.d(TAG,"disconnected",e);
                        return;
                    }
                }
            }
        };
        receiveThread.start();
    }
    public void sendMsg(String str){
        str+="\n";
        try {
            mOutputStream.write(str.getBytes());
            mOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public BluetoothSocket getBluetoothSocket() {
        return mBluetoothSocket;
    }

    public String getName() {
        return mBluetoothSocket.getRemoteDevice().getName();
    }

    public String getTake(){
        try {
            return mMsg.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getMsgSize() {
        return mMsg.size();
    }

    public boolean isConnected() {
        return mBluetoothSocket.isConnected();
    }

    public void close() {
        try {
            mBluetoothSocket.close();
        } catch (IOException e) {
            Log.d(TAG,"socket closed");
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
}