package com.example.jihwa.androidbluetoothwithbluecoveprac;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.jihwa.androidbluetoothwithbluecoveprac.protocol.CRC16;
import com.example.jihwa.androidbluetoothwithbluecoveprac.protocol.DataProcess;
import com.example.jihwa.androidbluetoothwithbluecoveprac.protocol.AnalysisProtocolHeader;
import com.example.jihwa.androidbluetoothwithbluecoveprac.protocol.DataSenderOnlyTest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;



/**
 * Created by jihwa on 2017-05-15.
 */

public class SocketManager {
    private static final String TAG = "BluetoothServer";
    private static final boolean TEST = true;

    // 받는 메세지가 저장될 큐
    private static final ArrayBlockingQueue<String> mMsgQueue = new ArrayBlockingQueue<>(20);

    private final BluetoothAdapter mLocalDevice;

    private BluetoothServerSocket mBluetoothServerSocket;
    private volatile BluetoothSocket mBluetoothSocket = null;

    private String mConnectedDeviceName;

    private InputStream mInputStream = null;
    private OutputStream mOutputStream = null;

    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private TextView mConnectionStatus;
    private ArrayAdapter<String> mConversationArrayAdapter;

    private MsgTask mMsgTask;

    public SocketManager(TextView textViewConnectionStatus, ArrayAdapter<String> conversationArrayAdapter){
        // TextView와 ArrayAdapter 모두 Activity가 아니라면, UI 변수를 가져올 수 없으므로, 인자로 받아와서 초기화한다.
        mConnectionStatus = textViewConnectionStatus;
        mConversationArrayAdapter = conversationArrayAdapter;
        // 현재 기기를 mLocalDevice에 초기화한다. ( 로컬 디바이스 반환 )
        // 만약, 블루투스 작동이 안되면, 생성자를 종료시킨다.
        mLocalDevice = BluetoothAdapter.getDefaultAdapter();
        if(mLocalDevice == null){
            Log.d(TAG,"This device is not implement Bluetooth.");
            return;
        }
        Log.d(TAG,"Initialisation successful.");

        // RFCOMM ssecure를 활용한 서버소켓을 만든다. 각각 서버이름과, UUID가 인자로 넘어간다.
        try {
            mBluetoothServerSocket = mLocalDevice.listenUsingRfcommWithServiceRecord("SDP NAME",MY_UUID);
        } catch (IOException e) {
            Log.e(TAG,"cannot create server mBluetoothSocket");
        }

        // 이렇게 안하면, 메인 스레드가 대기상태에서 계속유지되어, UI가 초기화되지 않음.
        // 메인 스레드가 UI를 업데이트시킨다.
        Thread thread = new Thread() {
            @Override
            public void run() {
                while(true) {
                    try {

                        //주석처리한 부분은 사용안함. 테스트해본것임 . 추후 사용가능할수도 있으니 남겨둠.
                        // MAC address를 이용한 블루투스 연결
                        //BluetoothDevice bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice("60:36:DD:8C:90:BE");
                        //mBluetoothSocket= bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
                        //mBluetoothSocket.connect();

                        // 만약, 연결이 종료되었다가 다시 돌아왔을수도 있으니, null로 만들어줌.
                        mBluetoothSocket = null;
                        Log.d(TAG, "waiting for new paired device");
                        // bluetoothServerSocket에 요청이 들어오면, 연결된 소켓을 bluetoothSocket에 초기화한다.
                        mBluetoothSocket = mBluetoothServerSocket.accept();
                        mConnectedDeviceName = mBluetoothSocket.getRemoteDevice().getName();
                        Log.d(TAG, "accept success. new connectTask . device = " + mConnectedDeviceName);

                        // 소켓에서 각각의 스트림을 열어준다.
                        try {
                            mInputStream = mBluetoothSocket.getInputStream();
                            mOutputStream = mBluetoothSocket.getOutputStream();
                        } catch (IOException e) {
                            Log.d(TAG, "mBluetoothSocket closed " + e.getMessage());
                        }
                        // receive message를 관리하기 위해 MsgTask를 선언하여 실행시킨다.
                        mMsgTask = new MsgTask();
                        mMsgTask.execute();

                        // msg를 받는 함수를 실행한다.
                        msgReceive();
                    } catch (IOException e) {
                        Log.d(TAG, "socket cannot accept!");
                    }
                }
            }
        };
        thread.start();
    }

    // 메세지를 받는 함수. 일반 메세지의 길이가 1010 byte를 넘기면, 메세지 처리중 에러가 뜬다.
    private void msgReceive() {
        // message send
        AnalysisProtocolHeader analysisProtocolHeader = null;
        while(true){
            try{
                if(!mBluetoothSocket.isConnected()) {
                    Log.d(TAG,"disconnected check in msgReceive");
                    return;
                }

                // 읽을 수 있는 byte length 를 확인함
                int bytesAvailable = mInputStream.available();

                // 만약,  헤더가 도착해있을 경우
                if(bytesAvailable >= AnalysisProtocolHeader.HEADER_LENGTH) {
                    // packet으로 온 bytes를 저장할 변수를 선언함
                    byte[] dataPackets = new byte[AnalysisProtocolHeader.HEADER_LENGTH];
                    //read from the inputStream
                    // mInputStream에서 읽어온 bytes를 packetBytes에 저장함.
                    mInputStream.read(dataPackets);
                    Log.d(TAG,"read packet");

                    analysisProtocolHeader = new AnalysisProtocolHeader(dataPackets);
                    Log.d(TAG,"id = " + analysisProtocolHeader.getId().toString() + " available =  " + bytesAvailable);

                    // 만약, 데이터 길이가 0보다 크면 조건걸어서 처리.
                    // data bytes가 존재하는것과 존재하지 않는것 미리 구분해놓기.
                    if (analysisProtocolHeader.analysisHeader()) {
                        Log.d(TAG,"analysis header and data process");
                        //  만약,  header에 저장되어있는 데이터의 길이가 0보다 클경우, data가 포함되어있으므로 아래를 실행함.
                        if(analysisProtocolHeader.getDataLength() > 0) {
                            while (analysisProtocolHeader.getDataLength() > mInputStream.available()) {

                            }
                            Log.d(TAG, "available length = " + mInputStream.available());

                            if (analysisProtocolHeader.getDataLength() <= mInputStream.available()) {
                                byte[] packetData = new byte[analysisProtocolHeader.getDataLength()];
                                mInputStream.read(packetData);
                                Log.d(TAG,"crc result = "+ analysisProtocolHeader.getCrc() + " , " + CRC16.getDataCRC(packetData) + "  ,   "
                                        + CRC16.checkData(analysisProtocolHeader.getCrc(),packetData));
                                Log.d(TAG, "packet Data length = " + packetData.length);
                                new DataProcess(analysisProtocolHeader.getId()).process(packetData);
                            }
                        } else {
                            new DataProcess(analysisProtocolHeader.getId()).process(null);
                        }
                    }else{
                        //  해당부분이 실행되면, 에러터진것. 헤더 분석이 제대로 안된것.
                    }
                }else if (bytesAvailable>0){
                    // 이부분도 에러터진것. header의 길이가 0보다크고 5보다 작은경우 발생. 주의
                }
            } catch (IOException e) {
                Log.d(TAG,"disconnected",e);
                return;
            }
        }
    }

    // 메세지를 보내는 method.
    // 해당 method는 MainActivity의 send버튼에서 호출되어 사용된다.
    public void sendMessage(String sendMessage) {
        // 만약 빈 문자열이면 그냥 종료시킨다.
        if("".equals(sendMessage))
            return;

        // 보내는 메세지를 ArrayAdapter에 추가시키고, 마지막에 \n을 붙여준다.
        mConversationArrayAdapter.insert("YOU" + ": " + sendMessage, 0);
        try {
            // 파일을 byte 타입으로 변환하여 보내준다.
            if(TEST && sendMessage.length() == 4){
                Log.d(TAG,"send Temp" + sendMessage);
                new DataSenderOnlyTest(mOutputStream).sendData(Environment.getExternalStorageDirectory().getAbsolutePath() + "/TestPath/temp.txt");
            }else{
//                Log.d(TAG,"sendMessage" + sendMessage);
//                sendMessage+="\n";
//                mOutputStream.write(sendMessage.getBytes());
                if(mOutputStream != null)
                    mOutputStream.flush();
            }
        } catch (IOException e) {
            // 만약, 스트림 연결이 안되어있으면, 해당 익셉션 처리 후, 소켓을 닫아준다.
            Log.d(TAG,"send message : broken pipe - " + e.getMessage());
            try {
                if(mBluetoothSocket != null)
                    mBluetoothSocket.close();
            } catch (IOException e1) {
                Log.d(TAG,"disconnect check in sendMessage method");
            }
        }
    }


    // message 처리부분
    private class MsgTask extends AsyncTask<Void,String,Void> {
        public MsgTask() {
        }

        // Message를 인자로 받아서 UI를 업데이트시켜주는 부분.
        @Override
        protected void onProgressUpdate(String... values) {
            mConnectionStatus.setText("Connected to " + mConnectedDeviceName);
            if(values != null) {
                Log.d(TAG, "GET MSG = " + values[0]);
                mConversationArrayAdapter.insert(mConnectedDeviceName + ": " + values[0], 0);
            }
        }

        // 계속해서 msgQueue 에 메세지가 쌓여있는지 확인한 후에, , UI 업데이트를 콜하는 부분
        @Override
        protected Void doInBackground(Void... params) {
            publishProgress(null);
            if(mBluetoothSocket != null)
                while(mBluetoothSocket.isConnected()){
                    if(mMsgQueue.size()>0) {
                        String str = null;
                        try {
                            str = mMsgQueue.take();
                        } catch (InterruptedException e) {
                            Log.e(TAG,"interrupt exception in msg take - " + e.getMessage());
                        }
                        if(str!=null)
                            publishProgress(str);
                        if(mBluetoothSocket != null)
                            if(!mBluetoothSocket.isConnected())
                                return null;
                    }
                }
            return null;
        }

        // doInBackground가 종료될 경우 호출하여, task를 cancel하는 부분
        @Override
        protected void onPostExecute(Void aVoid) {
            // 연결이 종료되거나 어플이 종료되면
            // 상태 메세지를 null로 만든다.
            mConnectionStatus.setText("Connected to " + null);
            // 쌓여있는 ArrayAdapter를 clear 한다.
            mConversationArrayAdapter.clear();
            // socket을 닫는다.
            try {
                if(mBluetoothSocket != null)
                    mBluetoothSocket.close();
            } catch (IOException e) {
                Log.d(TAG,"mBluetoothSocket closed");
            }
            // task를 취소시킨다.
            cancel(true);
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
            Log.d(TAG,"task cancel check");
        }
    }


    // Activity에서 호출하며, MsgTask와, BluetoothSocket을 cancel 하고 close 하기 위해 사용.
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

    public static ArrayBlockingQueue<String> getmMsgQueue() {
        return mMsgQueue;
    }
}