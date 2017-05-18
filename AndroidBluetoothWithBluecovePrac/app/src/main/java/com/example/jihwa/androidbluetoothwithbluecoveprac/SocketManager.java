package com.example.jihwa.androidbluetoothwithbluecoveprac;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;



/**
 * Created by jihwa on 2017-05-15.
 */

public class SocketManager {
    private static final String TAG = "BluetoothServer";
    private final BluetoothAdapter mLocalDevice;
    private BluetoothServerSocket mBluetoothServerSocket;
    private volatile BluetoothSocket mBluetoothSocket = null;
    private String mConnectedDeviceName;
    private InputStream mInputStream = null;
    private OutputStream mOutputStream = null;
    private ArrayBlockingQueue<String> mMsg = new ArrayBlockingQueue<>(20);

    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private TextView mConnectionStatus;
    private ArrayAdapter<String> mConversationArrayAdapter;

    private MsgTask mMsgTask;

    private boolean isReceiveFile = false;

    // 파일 전송받을때, 처음엔 1초간 대기하고, 스트림을 각각 읽을때마다 100ms만큼 대기함.
    private final int fileReceiveStartDelayTime = 1000;
    private int fileReceiveDelayTime = 100;

    // TODO : 버튼을 누를경우, dialog를 띄워서 delay Time을 조정할 수 있게 만듬.
    private int delayTimeSelect = 0;
    private int delayTimes[] = {50,100,150,200,250,300,350,400,450,500};

    public SocketManager(TextView textViewConnectionStatus, ArrayAdapter<String> conversationArrayAdapter){
        mConnectionStatus = textViewConnectionStatus;
        mConversationArrayAdapter = conversationArrayAdapter;
        // 로컬어뎁터를 반환한다.
        mLocalDevice = BluetoothAdapter.getDefaultAdapter();
        if(mLocalDevice == null){
            Log.d(TAG,"This device is not implement Bluetooth.");
            return;
        }
        Log.d(TAG,"Initialisation successful.");

        // 2. 페어링 되어 있는 블루투스 장치들의 목록을 보여줌
        // 3. 목록에서 블루투스 장치를 선택하면 선택한 디바이스를
        // 인자로 하여 doConnect 함수가 호출.
        try {
            mBluetoothServerSocket = mLocalDevice.listenUsingRfcommWithServiceRecord("SDP NAME",MY_UUID);
        } catch (IOException e) {
            Log.e(TAG,"cannot create server mBluetoothSocket");
        }

        // 이렇게 안하면, 대기상태에서 계속유지되어, UI가 초기화되지 않음.
        Thread thread = new Thread() {
            @Override
            public void run() {
                while(true) {
                    try {
                        mBluetoothSocket = null;
                        Log.d(TAG, "waiting for new paired device");
                        //주석처리한 부분은 사용안함. 테스트해본것임 .
                        // MAC address를 이용한 블루투스 연결
                        //BluetoothDevice bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice("60:36:DD:8C:90:BE");
                        mBluetoothSocket //= bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
                                = mBluetoothServerSocket.accept();
                        //mBluetoothSocket.connect();
                        mConnectedDeviceName = mBluetoothSocket.getRemoteDevice().getName();
                        Log.d(TAG, "accept success. new connectTask . device = " + mConnectedDeviceName);


                        try {
                            mInputStream = mBluetoothSocket.getInputStream();
                            mOutputStream = mBluetoothSocket.getOutputStream();
                        } catch (IOException e) {
                            Log.d(TAG, "mBluetoothSocket closed " + e.getMessage());
                        }
                        mMsgTask = new MsgTask();
                        mMsgTask.execute();
                        msgReceive();
                    } catch (IOException e) {
                        Log.d(TAG, "socket cannot accept!");
                    }
                }
            }
        };
        thread.start();
    }

    //
    private void msgReceive() {
        // message send
        byte[] readBuffer = new byte[1024];
        int readBufferPosition = 0;
        File file = null;
        while(true){
            try{
                if(!mBluetoothSocket.isConnected()) {
                    Log.d(TAG,"disconnected check in msgReceive");
                    return;
                }
                // 근데 왜또되냐 진짜 미치겠네.
                // 읽을 수 있는 bytes 를 확인함
                // 아래는 주석 두줄은 구글번역기
                // 블록하지 않고이 입력 Stream로부터 읽어 들일 수가있는 (또는 스킵 할 수있다)
                // 바이트 수의 추정치. 입력 Stream의 마지막에 이르렀을 때의 바이트 수.
                int bytesAvailable = mInputStream.available();
                // 만약, 대기중인게 있을 경우,
                if(bytesAvailable>0) {
                    // packet으로 온 bytes를 저장할 변수를 선언함
                    byte[] packetBytes = new byte[bytesAvailable];
                    //read from the inputStream
                    // mInputStream에서 읽어온 bytes를 packetBytes에 저장함.
                    mInputStream.read(packetBytes);
                    if (isReceiveFile) {
                        // 이부분부터 파일수신부
                        // path directory 가 존재하지 않으면, directory 를 생성한 후에 파일을저장함
                            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TestPath";
                            file = new File(path);
                            file.mkdirs();
                            file = new File(path + "/" + new Date() + ".txt");
                            //file.createNewFile();
                            //FileOutputStream(file) 사용하면, permission denied 에러가뜸
                            // > permission 허가를 안해줘서그럼
                             FileOutputStream out = new FileOutputStream(file);
                            int readNum = 0;
                            //  > packetBytes로 값을 안읽어줘서 뻘짓함.
                            // 데이터가 너무 빨리전송되면, 에러터지는듯
                            // 만약, 전송내역에 원래 받아야하는 파일내용이 나타나면, 받는 딜레이타임을 조정해야한다.
                            try {
                                Thread.sleep(fileReceiveStartDelayTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            while(bytesAvailable > 0) {
                                out.write(packetBytes);
                                Log.d(TAG, "File Read : " + readNum++ + "\t available - " + bytesAvailable);
                                out.flush();
                                bytesAvailable = mInputStream.available();
                                packetBytes = new byte[bytesAvailable];
                                mInputStream.read(packetBytes);
                                try {
                                    Thread.sleep(fileReceiveDelayTime);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            // 파일 전송이 종료되면 해당내용을 UI에 추가해주고, 반대편에도 이것을 알린다.
                            mMsg.add("Success File Receive!");
                            Log.d(TAG, "file write[" + file.getPath() + "] Name[" + file.getName() + "] fileSize = " + file.length()
                                    + " byteSize = " + packetBytes.length);
                            isReceiveFile = false;

                    }else{
                        // 추정된 바이트의 길이보다 작을동안 실행.
                        for (int i = 0; i < bytesAvailable; i++) {
                            // b를 packetBytes 의 i번째 byte로 초기화시킴.
                            byte b = packetBytes[i];
                            // 만약, 엔터 이스케이프 시퀸스가 발견될 경우,
                            // 읽어들인 readBufferPosition의 크기를 지닌 encodedBytes를 선언하는데,
                            // 해당 변수는 결과적으로 readBuffer에 저장되어있는 모든 배열을 카피해감.
                            // TODO : check . why arraycopy readBuffer to encodedBytes
                            if (b == '\n') {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                // encodedBytes에 저장되어있는 값을 UTF-8 인코딩으로 recvMessage에
                                // 저장함.
                                String recvMessage = new String(encodedBytes, "UTF-8");


                                // readBufferPosition에 0을 대입함.
                                readBufferPosition = 0;

                                if (recvMessage.equals("sendFile")) {
                                    isReceiveFile = true;
                                    Log.d(TAG,"Send File set1 " + isReceiveFile);
                                }
                                Log.d(TAG,"Send File set2 " + recvMessage.equals("sendFile") + " length = "
                                        + recvMessage.length() + " sendFile " + ("sendFile".length()));
                                mMsg.add(recvMessage);
                            } else {
                                // 그렇지 않으면,
                                // readBuffer에 readBufferPostion의 위치에 b를 저장함.
                                readBuffer[readBufferPosition++] = b;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                Log.d(TAG,"disconnected",e);
                return;
            }
        }
    }

    public void sendMessage(String sendMessage) {
        if("".equals(sendMessage))
            return;
        mConversationArrayAdapter.insert("YOU" + ": " + sendMessage, 0);
        sendMessage+="\n";
        try {
            mOutputStream.write(sendMessage.getBytes());
            mOutputStream.flush();
        } catch (IOException e) {
            Log.d(TAG,"send message : broken pipe - " + e.getMessage());
            try {
                mBluetoothSocket.close();
            } catch (IOException e1) {
                Log.d(TAG,"disconnect check in sendMessage method");
            }
        }
    }

    public int getMsgSize() {
        return mMsg.size();
    }

    public boolean isConnected() {
        return mBluetoothSocket.isConnected();
    }

    public void close() {
        try {
            if(mBluetoothSocket != null)
                mBluetoothSocket.close();
        } catch (IOException e) {
            Log.d(TAG,"mBluetoothSocket closed");
        }
    }

    // message 처리부분
    private class MsgTask extends AsyncTask<Void,String,Void> {
        public MsgTask() {
        }

        // Message를 인자로 받아서 UI를 업데이트시켜주는 부분.
        // 만약, 파일이올경우 다른행동을함.
        @Override
        protected void onProgressUpdate(String... values) {
            mConnectionStatus.setText("Connected to " + mConnectedDeviceName);
            if(values != null) {
                Log.d(TAG, "GET MSG = " + values[0]);
                mConversationArrayAdapter.insert(mConnectedDeviceName + ": " + values[0], 0);
            }
        }

        // 계속해서 메세지가 왔는지 여부를 체크해서, UI 업데이트를 콜하는 부분
        @Override
        protected Void doInBackground(Void... params) {
            publishProgress(null);
            while(mBluetoothSocket.isConnected()){
                if(getMsgSize()>0) {
                    String str = null;
                    try {
                        str = mMsg.take();
                    } catch (InterruptedException e) {
                        Log.e(TAG,"interrupt exception in msg take - " + e.getMessage());
                    }
                    if(str!=null)
                        publishProgress(str);
                    if(!mBluetoothSocket.isConnected())
                        return null;
                }
            }
            return null;
        }

        // doInBackground가 종료될 경우 호출하여, task를 cancel하는 부분
        @Override
        protected void onPostExecute(Void aVoid) {
            mConnectionStatus.setText("Connected to " + null);
            mConnectionStatus.clearComposingText();
            cancel(true);
            close();
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
            Log.d(TAG,"task cancel check");
        }
    }

    public boolean isReceiveFile(){
        return isReceiveFile;
    }

    public void onCancel(){
        if(mMsgTask != null)
            if(!mMsgTask.isCancelled())
                mMsgTask.cancel(true);
        try {
            if(mBluetoothSocket!= null)
                mBluetoothSocket.close();
        } catch (IOException e) {
        }
    }
}