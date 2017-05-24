package com.example.jihwa.androidbluetoothwithbluecoveprac;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.jihwa.androidbluetoothwithbluecoveprac.protocol.DataProcess;
import com.example.jihwa.androidbluetoothwithbluecoveprac.protocol.Id;
import com.example.jihwa.androidbluetoothwithbluecoveprac.protocol.StartFlag;

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

    // 받는 메세지가 저장될 큐
    private ArrayBlockingQueue<String> mMsgQueue = new ArrayBlockingQueue<>(20);

    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private TextView mConnectionStatus;
    private ArrayAdapter<String> mConversationArrayAdapter;

    private MsgTask mMsgTask;

    private boolean isReceiveFile = false;

    // 파일 전송받을때, 처음엔 1초간 대기하고, 스트림을 각각 읽을때마다 100ms만큼 대기함.
    //약 350kb 기준 3.3초정도
    private final int fileReceiveStartDelayTime = 1000;
    private int fileReceiveDelayTime = 100;


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
        byte[] readBuffer = new byte[1024];
        int readBufferPosition = 0;
        File file = null;
        FileOutputStream out = null;
        ProtocolHeader protocolHeader = null;
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
                if(bytesAvailable >= protocolHeader.HEADER_LENGTH) {
                    // packet으로 온 bytes를 저장할 변수를 선언함
                    byte[] dataPackets = new byte[protocolHeader.HEADER_LENGTH];
                    //read from the inputStream
                    // mInputStream에서 읽어온 bytes를 packetBytes에 저장함.
                    mInputStream.read(dataPackets);
                    Log.d(TAG,"read packet");

                    protocolHeader = new ProtocolHeader(dataPackets);
                    Log.d(TAG,"id = " + protocolHeader.getId().toString() + " available =  " + bytesAvailable);
                    for(int i = 0 ; i < dataPackets.length ; i ++)
                        Log.d(TAG," value = " + dataPackets[i]);
                    //
                    // 만약, 데이터 길이가 0보다 크면 조건걸어서 처리.
                    // data bytes가 존재하는것과 존재하지 않는것 미리 구분해놓기.
                    // enum Id 에 해당 부분을
                    // newFile. writeFile, endFile
                    //
                    //
                    if (protocolHeader.analysisHeader()) {
                        Log.d(TAG,"analysis header");
                        // 분석됐으면 동작하기.


                        Log.d(TAG,"this order = " + protocolHeader.getStartFlag().toString());
                        //  만약,  header에 저장되어있는 데이터의 길이가 0보다 클경우, data가 포함되어있으므로 아래를 실행함.
                        if(protocolHeader.getDataLength() > 0) {
                            while (protocolHeader.getDataLength() > mInputStream.available()) {

                            }
                            Log.d(TAG, "available length = " + mInputStream.available());

                            if (protocolHeader.getDataLength() <= mInputStream.available()) {
                                byte[] packetData = new byte[protocolHeader.getDataLength()];
                                mInputStream.read(packetData);
                                Log.d(TAG, "packet Data length = " + packetData.length);
                                new DataProcess(protocolHeader.getId(), packetData).getProcess().process(packetData);
                            }
                        }
                        else {
                            new DataProcess(protocolHeader.getId()).getProcess().process(null);
                        }

                    }
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
        sendMessage+="\n";
        try {
            // 파일을 byte 타입으로 변환하여 보내준다.

            mOutputStream.flush();
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
}