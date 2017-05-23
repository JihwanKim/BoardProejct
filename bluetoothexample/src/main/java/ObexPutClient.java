import com.sun.istack.internal.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.util.Date;
import java.util.Scanner;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

public class ObexPutClient {

    public static void main(String[] args) throws IOException, InterruptedException {

        String serverURL = null; // = "btgoep://0019639C4007:6";
//        if ((args != null) && (args.length > 0)) {
//            serverURL = args[0];
//        }
//        if (serverURL == null) {
//            String[] searchArgs = null;
//            // Connect to OBEXPutServer from examples
//             searchArgs = new String[] { "0000110100001000800000805F9B34FB" };
//            ServicesSearch.main(searchArgs);
//            if (ServicesSearch.serviceFound.size() == 0) {
//                System.out.println("OBEX service not found");
//                return;
//            }
//            // Select the first service found
//            serverURL = (String)ServicesSearch.serviceFound.elementAt(0);
//        }
        serverURL = "btspp://F8E61A466934:5;authenticate=false;encrypt=false;master=false";
        //sppbtspp://F8E61A466934:5;authenticate=false;encrypt=false;master=false
        //String value = "btspp://F8E61A466934:5;authenticate=false;encrypt=false;master=false";
        System.out.println("connect url for spp"+serverURL);
        // 참고링크
        // http://stackoverflow.com/questions/15343369/sending-a-string-via-bluetooth-from-a-pc-as-client-to-a-mobile-as-server
        final StreamConnection streamConnection = (StreamConnection) Connector.open(serverURL);
        InputStream inputStream = streamConnection.openInputStream();
        OutputStream outputStream = streamConnection.openOutputStream();
        Thread recvMsg = new Thread(new ReceiveThread(inputStream));
        Thread sendMsg = new Thread(new SendThread(streamConnection,outputStream));

        sendMsg.start();
        recvMsg.start();
    }

}

class SendThread implements Runnable{
    final OutputStream mOutputStream;
    final StreamConnection streamConnection;
    public SendThread(@NotNull StreamConnection stream,OutputStream outputStream) {
        streamConnection = stream;
        mOutputStream = outputStream;

    }
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(mOutputStream));
        while(true){
            try {
                System.out.print("보낼 메세지 : ");
                String sendMsg = reader.readLine();
                if(sendMsg.equals("\"end\"")){
                    streamConnection.openInputStream().close();
                    mOutputStream.close();
                    streamConnection.close();
                    return;
                }
                if(sendMsg.equals("sendFile")) {
                    Scanner scan = new Scanner(System.in);
                    File file = new File(scan.nextLine());
                    // TODO : protocol 을 사용해서 전송하는 부분 해보기
                    byte[] fileByte = Files.readAllBytes(file.toPath());
                    int sendByteLength = 0;
                    byte[] packet =JHProtocol.makePacket(JHProtocol.makeHeader(JHProtocol.StartFlag.DATA,JHProtocol.Id.DATA_NAME),file.getName().getBytes());
                    for(int i = 0 ; i < 5 ; i ++){
                        System.out.print(i + " = " +packet[i] +"\t");
                    }
                    System.out.println();
                    mOutputStream.write(packet);
                    mOutputStream.flush();

                    packet =JHProtocol.makePacket(JHProtocol.makeHeader(JHProtocol.StartFlag.DATA,JHProtocol.Id.DATA_BODY),fileByte);
                    for(int i = 0 ; i < 5 ; i ++){
                        System.out.print(i + " = " +packet[i] +"\t");
                    }
                    System.out.println();
                    mOutputStream.write(packet);
                    mOutputStream.flush();

                    packet =JHProtocol.makePacket(JHProtocol.makeHeader(JHProtocol.StartFlag.DATA,JHProtocol.Id.DATA_END),null);
                    for(int i = 0 ; i < 5 ; i ++){
                        System.out.print(i + " = " +packet[i] +"\t");
                    }
                    System.out.println();
                    mOutputStream.write(packet);
                    mOutputStream.flush();

                    System.out.println("loop num = " +" byteLength = " + sendByteLength);


                    mOutputStream.flush();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mOutputStream.flush();

                    System.out.println("sendFile TOTAL - " + file.length());
                }else{
                    //mOutputStream.write(JHProtocol.combineProtocolAndBody(JHProtocol.Name.SMSG,(sendMsg+"\n").getBytes()));
                    mOutputStream.flush();
                }

                System.out.println("["+new Date()+"]" + new String(sendMsg));
            } catch (IOException e) {
                try {
                    mOutputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return;
            }
        }
    }
}


class ReceiveThread implements Runnable{
    final InputStream mInputStream;
    public ReceiveThread(@NotNull InputStream stream) {
        mInputStream = stream;
    }
    public void run() {
        while(true){
            String sb;
            BufferedReader bReader2=new BufferedReader(new InputStreamReader(mInputStream));
            while(true){
                try{
                    sb = (bReader2.readLine());
                    if(sb!=null)
                        System.out.println("["+ new Date() +"] ReceiveMsg = "+sb);
                } catch (IOException e) {
                    try {
                        mInputStream.close();
                    } catch (IOException e1) {
                    }
                }
            }
        }
    }
}