package com.example.jihwa.androidbluetoothwithbluecoveprac;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
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
    private final Context mContext;
    private BluetoothServerSocket mBluetoothServerSocket;
    private BluetoothSocket mBluetoothSocket = null;
    private String mConnectedDeviceName;
    private InputStream mInputStream = null;
    private OutputStream mOutputStream = null;
    private ArrayBlockingQueue<String> mMsg = new ArrayBlockingQueue<>(20);

    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private TextView mConnectionStatus;
    private ArrayAdapter<String> mConversationArrayAdapter;

    private MsgTask mMsgTask;

    private boolean isReceiveFile = false;
    private File file = null;
    private FileOutputStream mFileOutputStream;


    public SocketManager(TextView textViewConnectionStatus, ArrayAdapter<String> conversationArrayAdapter, Context context){
        mContext = context;
        mConnectionStatus = textViewConnectionStatus;
        mConversationArrayAdapter = conversationArrayAdapter;
        // 로컬어뎁터를 반환한다.
        mLocalDevice = BluetoothAdapter.getDefaultAdapter();
        if(mLocalDevice == null){
            //showErrorDialog("This device is not implement Bluetooth.");
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
                try {
                    Log.d(TAG, "waiting for new paired device");
                    mBluetoothSocket = mBluetoothServerSocket.accept();
                    mConnectedDeviceName = mBluetoothSocket.getRemoteDevice().getName();
                    Log.d(TAG, "accept success. new connectTask . device = " + mConnectedDeviceName);

                    mMsgTask = new MsgTask();
                    mMsgTask.execute();

                    try {
                        mInputStream = mBluetoothSocket.getInputStream();
                        mOutputStream = mBluetoothSocket.getOutputStream();
                    } catch (IOException e) {
                        Log.d(TAG,"mBluetoothSocket closed " + e.getMessage());
                    }
                    msgReceive();
                } catch (IOException e) {
                    Log.d(TAG,"socket cannot accept!");
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
        while(true){
            try{
                if(!mBluetoothSocket.isConnected()) {
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
                        //int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.);

                        int c;
                        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                        file = new File(path,"test.txt");

                        FileOutputStream out = new FileOutputStream(file);//mContext.openFileOutput("test.txt",Context.MODE_PRIVATE);
                        out.write(packetBytes);
                        out.flush();
                        out.close();
                        Log.d(TAG,"file write[" + file.getPath() + "] Name[" + file.getName() + "] fileSize = " + file.length());
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
                            }
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
        mConversationArrayAdapter.insert("YOU" + ": " + sendMessage, 0);
        sendMessage+="\n";
        try {
            mOutputStream.write(sendMessage.getBytes());
            mOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
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
            Log.d(TAG, "GET MSG = " + values[0]);
            mConversationArrayAdapter.insert(mConnectedDeviceName + ": " + values[0], 0);

        }

        // 계속해서 메세지가 왔는지 여부를 체크해서, UI 업데이트를 콜하는 부분
        @Override
        protected Void doInBackground(Void... params) {
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
                }
            }
            return null;
        }

        // doInBackground가 종료될 경우 호출하여, task를 cancel하는 부분
        @Override
        protected void onPostExecute(Void aVoid) {
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

    private void receiveFile(byte [] bytes){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        file = new File(path + "/"+new Date()+".txt");
        Log.d(TAG,"path = " + file.getPath());
        try {
            mFileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            Log.d(TAG,"bytes = " + bytes.length);
            byte [] byte2 = "123213123".getBytes();
            mFileOutputStream.write(byte2);

            mFileOutputStream.flush();
            mFileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        file = null;
        mFileOutputStream = null;

        isReceiveFile = false;
    }
    public void onCancel(){
        try {
            mBluetoothSocket.close();
        } catch (IOException e) {
        }
        if(!mMsgTask.isCancelled())
            mMsgTask.cancel(true);
    }
}