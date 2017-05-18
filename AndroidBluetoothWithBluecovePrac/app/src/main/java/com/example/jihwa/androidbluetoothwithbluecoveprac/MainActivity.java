package com.example.jihwa.androidbluetoothwithbluecoveprac;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
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
    private final int REQUEST_FILE_ENABLE = 100;
    // 현재 연결 상태를 보여주는 TextView
    private TextView mConnectionStatus;
    // 쓰는 글을 입력받을 EditText
    private EditText mInputEditText;

    // BluetoothAdapt를 선언하는데, 로컬어뎁터가 초기화되어 사용한다.
    static BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mConversationArrayAdapter;

    private static final String TAG = "MainActivity";

    private SocketManager socketManager;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button sendButton = (Button) findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String sendMessage = mInputEditText.getText().toString();
                if(sendMessage.length()>0 && socketManager!=null && !socketManager.isReceiveFile()) {
                    Log.d(TAG,"SEND MSG :" +sendMessage);
                    socketManager.sendMessage(sendMessage);
                    mInputEditText.setText(" ");
                }
            }
        });
        mConnectionStatus = (TextView) findViewById(R.id.connection_status_textview);
        mInputEditText = (EditText) findViewById(R.id.input_string_edittext);
        ListView mMessageListView = (ListView) findViewById(R.id.message_listview);

        mConversationArrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_activated_1);
        mMessageListView.setAdapter(mConversationArrayAdapter);
        context = getApplicationContext();
        callSocketManager();
    }

    private void callSocketManager(){
        //파일 쓰기 권한요청
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_FILE_ENABLE);
        // bluetooth adapter 초기화
        // 만약, off면, on으로 바꾸고, SocketManager실행
        Log.d(TAG,"Initializing Bluetooth adapter..");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!mBluetoothAdapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,REQUEST_BLUETOOTH_ENABLE);
        }else {
            socketManager = new SocketManager(mConnectionStatus, mConversationArrayAdapter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socketManager.onCancel();
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
                callSocketManager();
            }
            if(resultCode == RESULT_CANCELED){
                showQuitDialog("You need to enable bluetooth");
            }
        }
    }

    // 권한허용 요청 결과에대한 실행
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_FILE_ENABLE){
            if(requestCode == RESULT_OK){
                //nothing
            }
            if(requestCode == RESULT_CANCELED){
                onDestroy();
            }
        }
    }
}
