package com.example.jihwa.androidbluetoothwithbluecoveprac;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
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

    //
    ConnectedTask mConnectedTask = null;

    // BluetoothAdapt를 선언하는데, 로컬어뎁터가 초기화되어 사용한다.
    static BluetoothAdapter mBluetoothAdapter;
    private String mConnectedDeviceName = null;
    private ArrayAdapter<String> mConversationArrayAdapter;
    static boolean isConnectionError = false;
    private static final String TAG = "BluetoothClient";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button sendButton = (Button) findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String sendMessage = mInputEditText.getText().toString();
                if(sendMessage.length()>0)
                    sendMessage(sendMessage);
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
            showPairedDevicesListDialog();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mConnectedTask!= null){
            mConnectedTask.cancel(true);
        }
    }

    //
    private class ConnectTask extends AsyncTask<Void,Void,Boolean>{
        private BluetoothSocket mBluetoothSocket = null;
        private BluetoothDevice mBluetoothDevice = null;

        public ConnectTask(BluetoothDevice bluetoothDevice) {

            // UUID를 설정한다. 해당 설정을 통해 블루투스 프로토콜을 지정해준다.
            // SerialPortServiceClass_UUID
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            // 멤버변수 mBluetoothDevice 에 param bluetoothDevice 를 초기화한다.
            // param 변수는 dialog에서 선택된 디바이스다.
            mBluetoothDevice = bluetoothDevice;
            // 멤버변수 mConnectedDeviceName 에 mBluetoothDevice 의 이름을 초기화한다.
            mConnectedDeviceName = mBluetoothDevice.getName();
            //get a bluetoothSocket for a connection with the
            // given bluetoothDevice
            try{
                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);

                Log.d(TAG,"create socket for "+mConnectedDeviceName);
            } catch (IOException e) {
                Log.d(TAG,"socket create failed " + e.getMessage());
            }
            mConnectionStatus.setText("Connecting...");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // Always cancel discovery because it will slow down a connection
            // 연결이 느려지기 때문에, 항상 발견하던것을 취소함.
            mBluetoothAdapter.cancelDiscovery();
            try{
                mBluetoothSocket.connect();
                Log.e(TAG,"mBluetoothSocket connect");
            } catch (IOException e) {
                try{
                    mBluetoothSocket.close();
                } catch (IOException e1) {
                    Log.e(TAG,"unable to close()" + "socket during connection failure",e1);
                    return false;
                }
            }
            return true;
        }

        // doInBackground 가 종료되면, 해당 method 의 반환값을 인자로 사용한다.
        // 만약 결과가 true라면, mBluetoothSocket을 connect 한다.
        @Override
        protected void onPostExecute(Boolean isSuccess) {
            if(isSuccess){
                connected(mBluetoothSocket);
            }else{
                isConnectionError = true;
                Log.d(TAG,"Unable to connect device");
                showErrorDialog("Unable to connect device");
            }
        }
    }

    // 연결된 기기를 실행시킨다. background로 넘겨서 실행시킨다.
    private void connected(BluetoothSocket mBluetoothSocket) {
        mConnectedTask = new ConnectedTask(mBluetoothSocket);
        mConnectedTask.execute();
    }


    // this thread runs during a connection with a remote device.
    // it handles all incoming and outgoing transmissions.
    // 이 스레드는 상대 디바이스와 연결되어 있는동안만 실행됨.
    // 이 핸들은 들어오는것과 나가는 것들의 전송을 관리함.
    private class ConnectedTask extends  AsyncTask<Void,String,Boolean>{
        private InputStream mInputStream = null;
        private OutputStream mOutputStream = null;
        private BluetoothSocket mBluetoothSocket = null;

        public ConnectedTask(BluetoothSocket socket) {
            // 멤버변수 mBluetoothSocket에 socket 으로 초기화시켜줌.
            mBluetoothSocket = socket;
            try{
                // 각 멤버변수에 mBLuetoothSocket에 inputStream과 outputStream을 초기화시켜줌
                mInputStream = mBluetoothSocket.getInputStream();
                mOutputStream = mBluetoothSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG,"socket not created",e);
            }

            Log.d(TAG,"connected to " + mConnectedDeviceName);
            // 연결되어있는 상태를 표시하는 텍스트에 연결된 디바이스의 이름이 연결됐다고 뜨게함.
            mConnectionStatus.setText("connected to " + mConnectedDeviceName);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            byte[] readBuffer = new byte[1024];
            int readBufferPosition = 0;
            while(true){
                if(isCancelled()) return false;

                try{
                    // Error-Point java.io.IOException: socket closed
                    // 계속 disconnected 됨
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
                                Log.d(TAG,"recv message: " + recvMessage);
                                publishProgress(recvMessage);
                            }else{
                                // 그렇지 않으면,
                                // readBuffer에 readBufferPostion의 위치에 b를 저장함.
                                readBuffer[readBufferPosition++] = b;
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.d(TAG,"disconnected",e);
                    return false;
                }
            }
        }

        // UI를 업데이트 시키는 메서드.(?) super method를 호출할 필요 없음.
        @Override
        protected void onProgressUpdate(String... recvMassage) {
            //super.onProgressUpdate();
            mConversationArrayAdapter.insert(mConnectedDeviceName + ": " + recvMassage[0],0);
        }

        // doInBackground method가 종료되면, 인자로 결과값을 사용하고 method를 실행한다.
        // super class 의 onPostExecute 는 호출할 필요가 없다.
        @Override
        protected void onPostExecute(Boolean isSuccess) {
            if(!isSuccess){
                closeSocket();
                Log.d(TAG,"Device connection was lost");
                isConnectionError = true;
                showErrorDialog("Device connection was lost");
            }
        }

        // cancel thread 이후에 불러들이는 메서드인고 doInBackground를 종료한다. ??
        @Override
        protected void onCancelled(Boolean aBoolean) {
            closeSocket();
        }

        // 소켓을 닫는다.
        void closeSocket() {
            try{
                mBluetoothSocket.close();
                Log.d(TAG,"close socket()");
            } catch (IOException e) {
                Log.e(TAG,"unable to close()"
                        + "socket during connection failure",e);
            }
        }
        // 메세지를 전송한다.
        // inner class 가 아닌, 상위 클래스에서 해당 메서드를 호출한다.
        // use in sendMessage
        void write(String msg){
            msg += "\n";
            try{
                // flush하면 스트림에 쓰인 값들이 bytes 타입으로 전송된다.
                mOutputStream.write(msg.getBytes());
                mOutputStream.flush();
            } catch (IOException e) {
                Log.e(TAG,"Exception during send",e);
            }
            // 전송 이후, 인풋 텍스트를 비운다.
            mInputEditText.setText(" ");
        }
    }


    private void showPairedDevicesListDialog() {
        // Local device와 연결된 device들을 가져온다.
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();

        // to Array의 반환값과 인자가 T 제네릭 이기 때문에, new BluetoothDevice[0]를 사용한거 같다.
        // TODO : check again. why used new BluetoothDevice[0] in parameter.
        final BluetoothDevice[] pairedDevices = devices.toArray(new BluetoothDevice[0]);

        // 페어된 디바이스의 길이를 체크하여, 하나도 페어되지 않았다면, 종료 다이얼로그를 띄우고
        // method를 종료시킨다.
        if(pairedDevices.length == 0){
            showQuitDialog("NO device have been paired.\n" +
                    "You must pair it with another device.");
            return;
        }
        // pairedDevices 의 이름들이 들어갈 items String array를 선언하여 각각 초기화시켜준다.
        String[] items;
        items = new String[pairedDevices.length];
        for(int i = 0 ; i < pairedDevices.length ; i++){
            items[i] = pairedDevices[i].getName();
        }

        // Select device라는 AlertDialog를 선언하는데, Build 패턴을 사용하는거 같다.
        //
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select device");
        builder.setCancelable(false);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 작업이 종료되면, 클릭되면 현재다이얼로그를 아에 화면에서 제거한다.
                dialog.dismiss();

                // 선택된 paired Devices 를 인자로 ConnectTask를 선언하여, 실행시킨다.
                ConnectTask task = new ConnectTask(pairedDevices[which]);

                // this function schedules the task on a queue for a single background
                // * thread or pool of threads depending on the platform version
                task.execute();
            }
        });
        // builder를 만들고  display on screen
        builder.create().show();
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

    // 메세지를 전송한다.
    private void sendMessage(String msg) {
        if(mConnectedTask != null){
            mConnectedTask.write(msg);
            Log.d(TAG,"send message: " + msg);
            mConversationArrayAdapter.insert("Me: " + msg,0);
        }
    }

    // 블루투스 실행시키는 결과값을 확인하여, showPairedDevicesListDialog를 호출하거나,
    // 블루투스가 존재하지 않는 경우, 종료 다이얼로그를 보여준다.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_BLUETOOTH_ENABLE){
            if(resultCode == RESULT_OK){
                showPairedDevicesListDialog();
            }
            if(resultCode == RESULT_CANCELED){
                showQuitDialog("You need to enable bluetooth");
            }
        }
    }
}
