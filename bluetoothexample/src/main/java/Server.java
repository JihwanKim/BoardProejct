import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.*;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by jihwan on 2017-05-10.
 * 본 예제는 코드 이해를 위해 아래 블로그의 예제와 똑같이 작성함
 * http://webnautes.tistory.com/849
 */
public class Server {
    public static void main(String[] args) {
        log("local bluetooth device..\n");

        LocalDevice local = null;
        try{
            // an object that represents the local Bluetooth device
            // local never be null.
            local = LocalDevice.getLocalDevice();
        } catch (BluetoothStateException e) {
            e.printStackTrace();
        }
        //

        log("address: " + local.getBluetoothAddress());
        log("name" + local.getFriendlyName());
        Runnable r = new ServerRunnable();
        Thread thread = new Thread(r);
        thread.start();
    }
    private static void log(String msg){
        System.out.println("["+(new Date())+"]" +msg);
    }
}

class ServerRunnable implements Runnable{
    // uuid for ssp
    // bluetooth.UUID
    final UUID uuid = new UUID("0000110100001000800000805F9B34FB", false);
    final String CONNECT_URL_FOR_SPP = "btspp://localhost:"
            +uuid + ";name=SPP Server";
    private StreamConnectionNotifier mStreamConnectionNotifier = null;
    private StreamConnection mStreamConnection = null;
    private int count = 0;
    public void run() {
        try{
            // open의 파라미터 값을 가지고 새로운 Connection Obejct를 생성하여 반환
            // 정확히 어떤기능인지 documentation에 명시 안되어있음.\
            mStreamConnectionNotifier = (StreamConnectionNotifier) Connector.open(CONNECT_URL_FOR_SPP);
            log("Opened connection successful.");
        } catch (IOException e) {
            log("Could not open connection" + e.getMessage());
            return;
        }
        log("Server is now running");

        while(true){
            log("wait for client request");
            try{
                //StreamConnection that represents a server side socket connection
                // A socket to communicate with a client
                mStreamConnection = mStreamConnectionNotifier.acceptAndOpen();
            } catch (IOException e) {
                log("Could not open connection" + e.getMessage());
            }
            count++;
            log("접속중인 클라 수 : " + count);

            // 새로운 Receiver 쓰레드에 인자 mStreamConnection 을 넣어서 시작한다.
            new Receiver(mStreamConnection).start();
        }
    }


    private static void log(String msg){
        System.out.println("["+(new Date())+"]" +msg);
    }


    class Receiver extends Thread{
        private InputStream mInputStream = null;
        private OutputStream mOutputStream = null;
        private String mRemoteDeviceString = null;
        private StreamConnection mStreamConnection = null;
        private PrintWriter printWriter;

        private Thread outputStreamThread = new Thread(){
            @Override
            public void run() {
                Scanner scanner = new Scanner(System.in);
                while(true){
                    Sender(scanner.nextLine());
                }
            }
        };

        public Receiver(StreamConnection streamConnection) {
            mStreamConnection = streamConnection;
            try{
                // mStreamConnection 의 InputStream 과 OutputStream을 가져옴
                mInputStream = mStreamConnection.openInputStream();
                mOutputStream = mStreamConnection.openOutputStream();
            } catch (IOException e) {
                log("Could't open Stream : " + e.getMessage());
                try {
                    // 만약, 스트림을 열지 못하면, 혹시 열려있는 스트림이 있을경우 모두 닫음.
                    mOutputStream.close();
                    mInputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                // 현재 쓰레드를 중지시키고, 함수를 종료한다.
                Thread.currentThread().interrupt();
                return;
            }
            try{
                // mStreamConnection과 얽힌 remoteDevice를 가져옴
                RemoteDevice remoteDevice = RemoteDevice.getRemoteDevice(mStreamConnection);
                // remoteDevice의 블루투스 주소를 가져옴.
                mRemoteDeviceString = remoteDevice.getBluetoothAddress();
                log("Remote device");
                log("address: " + mRemoteDeviceString);
            } catch (IOException e) {
                log("Found device, but couldn't connect to it : " + e.getMessage());
                try {
                    mInputStream.close();
                    mOutputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return;
            }
            log("Client is connected...");
        }

        @Override
        public void run() {
            try{
                // Sender에서 일일히 PrintWriter를 계속해서 선언해주지않고, 한번만 선언하고 계속 재사용
                // 쓰기용 객체를 선언
                printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mOutputStream,"UTF-8")));
                // 읽기용 객체를 선언함.
                Reader mReader = new BufferedReader(new InputStreamReader(mInputStream,"UTF-8"));
                boolean isDisconnected = false;
                // 블루투스로 접속되어있는 기기에 해당 문자를 날림.
                Sender("에코 서버에 접속하셨습니다.");
                Sender("보내신 문자를 에코해 드립니다.");
                outputStreamThread.start();
                while(true){
                    log("ready");
                    StringBuilder stringBuilder = new StringBuilder();
                    int c = 0;
                    // c가 \n문자가 나오기 전까지 계속해서 읽어들이는데, 만약 c가 -1이라면 disconnected로 보고 해당 반복문을 중지
                    while('\n'!=(char)(c = mReader.read())){
                        if(c == -1){
                            log("Client has been disconnected");
                            count--;
                            log("현재 접속 중인 클라이언트 수 : " + count);
                            isDisconnected = true;
                            Thread.currentThread().interrupt();
                            break;
                        }
                        // 정상적으로 c값이 들어오면, stringBuilder 에 append함.
                        stringBuilder.append((char)c);
                    }
                    // 만약, disconect 상태면, while문 종료
                    if(isDisconnected) break;
                    // 메세지를 보냄
                    String recvMessage = stringBuilder.toString();
                    log(mRemoteDeviceString + ": " + recvMessage);

                    //Sender(recvMessage);

                }
            } catch (UnsupportedEncodingException e) {
                log("encoding error : " + e.getMessage());
            } catch (IOException e) {
                log("Receiver closed : " + e.getMessage());
            }finally {
                try {
                    mInputStream.close();
                    mOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 메세지를 날림.
        void Sender(String msg){
                printWriter.write(msg+"\n");
                printWriter.flush();
                log("Me : " + msg);
        }
    }
}
