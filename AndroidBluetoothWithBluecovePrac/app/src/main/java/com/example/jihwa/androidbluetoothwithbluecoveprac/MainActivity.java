package com.example.jihwa.androidbluetoothwithbluecoveprac;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.UUID;

/*
 * 본 예제에서 activity_main.xml 은 분석하지않고 그냥 복붙함
 * 원예제
 * http://webnautes.tistory.com/849
 * 원예제 - 참고
 * https://github.com/googlesamples/android-BluetoothChat
 * http://www.kotemaru.org/2013/10/30/android-bluetooth-sample.html
 */
public class MainActivity extends AppCompatActivity {

    private final int REQUEST_BLUETOOTH_ENABLE = 100;
    // 현재 연결 상태를 보여주는 TextView
    private TextView mConnectionStatus;
    // 쓰는 글을 입력받을 EditText
    private EditText mInputEditText;

    // BluetoothAdapt를 선언하는데, 로컬어뎁터가 초기화되어 사용한다.
    static BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mConversationArrayAdapter;

    static boolean isConnectionError = false;
    private static final String TAG = "BluetoothServer";
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private BluetoothServerSocket bluetoothServerSocket;
    private String mConnectedDeviceName;
    private SocketManager socketManager;

    private MsgTask mMsgTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button sendButton = (Button) findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String sendMessage = mInputEditText.getText().toString();
                if(sendMessage.length()>0 && socketManager!=null) {
                    Log.d(TAG,"SEND MSG :" +sendMessage);
                    sendMessage(sendMessage);
                    mInputEditText.setText(" ");
                }
            }
        });
        mConnectionStatus = (TextView) findViewById(R.id.connection_status_textview);
        mInputEditText = (EditText) findViewById(R.id.input_string_edittext);
        ListView mMessageListView = (ListView) findViewById(R.id.message_listview);

        mConversationArrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_activated_1);
        mMessageListView.setAdapter(mConversationArrayAdapter);

        Log.d(TAG,"Initializing Bluetooth adapter..");

        // 로컬어뎁터를 반환한다.
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            showErrorDialog("This device is not implement Bluetooth.");
            return;
        }

        if(!mBluetoothAdapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,REQUEST_BLUETOOTH_ENABLE);
        }else{
            Log.d(TAG,"Initialisation successful.");

            // 2. 페어링 되어 있는 블루투스 장치들의 목록을 보여줌
            // 3. 목록에서 블루투스 장치를 선택하면 선택한 디바이스를
            // 인자로 하여 doConnect 함수가 호출.
            try {
                bluetoothServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("SDP NAME",MY_UUID);
            } catch (IOException e) {
                Log.e(TAG,"cannot create server socket");
            }

            // 이렇게 안하면, 대기상태에서 계속유지되어, UI가 초기화되지 않음.
            Thread thread = new Thread() {
                @Override
                public void run() {
                    while(true) {
                        try {
                            BluetoothSocket socket = bluetoothServerSocket.accept();

                            socketManager = new SocketManager(socket);
                            mConnectedDeviceName = socketManager.getName();
                            Log.d(TAG, "accept success. new connectTask . device = " + mConnectedDeviceName);

                            mMsgTask = new MsgTask(socketManager);
                            mMsgTask.execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            thread.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMsgTask.cancel(true);
    }

    private void sendMessage(String sendMessage) {
        mConversationArrayAdapter.insert("YOU" + ": " + sendMessage, 0);
        socketManager.sendMsg(sendMessage);
    }


    private class MsgTask extends  AsyncTask<Void,String,Boolean>{
        SocketManager socketManager = null;
        public MsgTask(SocketManager socketManager) {
            this.socketManager = socketManager;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            mConnectionStatus.setText("Connected to " + mConnectedDeviceName);
            Log.d(TAG,"GET MSG = " + values[0]);
            mConversationArrayAdapter.insert(mConnectedDeviceName + ": " + values[0], 0);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            while(true){
                if(!socketManager.isRunning()) return false;
                if(socketManager.getMsgSize()>0) {
                    String str = socketManager.getTake();
                    publishProgress(str);
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            mMsgTask.cancel(true);
            mMsgTask = null;
            socketManager.close();
            socketManager = null;
        }
    }


    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quit");
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if(isConnectionError){
                    isConnectionError = false;
                    finish();
                }
            }
        });
        builder.create().show();
    }

    private void showQuitDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quit");
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.create().show();
    }


    // 블루투스 실행시키는 결과값을 확인하여, showPairedDevicesListDialog를 호출하거나,
    // 블루투스가 존재하지 않는 경우, 종료 다이얼로그를 보여준다.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_BLUETOOTH_ENABLE){
            if(resultCode == RESULT_OK){
            }
            if(resultCode == RESULT_CANCELED){
                showQuitDialog("You need to enable bluetooth");
            }
        }
    }
}
