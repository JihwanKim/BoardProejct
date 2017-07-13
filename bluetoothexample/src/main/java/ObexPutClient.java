import com.sun.istack.internal.NotNull;
import protocol.*;

import java.io.*;

// 해당오류 무시해도됨.
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
        Scanner scan = new Scanner(System.in);
        while(true){
            try {
                System.out.print("모드선택 ( 1. binary , 2. dose , 3. sendFile : ");
                String sendMsg = reader.readLine();
                if(sendMsg.equals("\"end\"")){
                    streamConnection.openInputStream().close();
                    mOutputStream.close();
                    streamConnection.close();
                    return;
                }

                // * 각 모드에서 데이터를 전송하는 것은
                // binary 모드
                if(sendMsg.equals("1")){
                    System.out.print("보낼 binary 값 ( 문자열의 좌측 문자가 MSB. 가장 우측 문자가 LSB. 해당 문자열은 오직 0과 1로만 이루어져야 합니다. 헤더와 crc값 모두 포함되어야합니다.) : ");
                    String str = scan.nextLine();
                    byte[] binaryBytes = new byte[str.length()%4!=0?str.length()/4+1:str.length()];
                    int arrayIndex = -1;
                    for(int i = 0 ; i < str.length(); i++){
                        final int shift = i%8;
                        if(shift == 0){
                            arrayIndex++;
                        }
                        binaryBytes[arrayIndex] += ((str.charAt(i) - '0') << shift);
                    }
                    mOutputStream.write(binaryBytes);
                    mOutputStream.flush();
                    System.out.println("Sending Data = " + new String(binaryBytes));
                } else if(sendMsg.equals("2")){
                    // dose 모드
                    System.out.print(" 보낼 Dose 데이터 값 ( 문자열의 좌측 문자가 MSB. 가장 우측 문자가 LSB. 해당 문자열은 오직 0과 1로만 이루어져야 합니다. )");
                    String str = scan.nextLine();
                    byte[] binaryBytes = new byte[str.length()%4!=0?str.length()/4+1:str.length()];
                    int arrayIndex = -1;
                    for(int i = 0 ; i < str.length(); i++){
                        final int shift = i%8;
                        if(shift == 0){
                            arrayIndex++;
                        }
                        binaryBytes[arrayIndex] += ((str.charAt(i) - '0') << shift);
                    }
                    byte[] packet = new CreateProtocol(HeaderStartFlag.DOSE,HeaderId.DOSE_DATA,binaryBytes).toProtocol();
                    mOutputStream.write(packet);
                    mOutputStream.flush();
                } else if(sendMsg.equals("3")) {
                    // file send mode
                    System.out.print("보낼 파일명 확장자를 포함해야합니다.: ");
                    File file = new File(scan.nextLine());
                    // TODO : protocol 을 사용해서 전송하는 부분 해보기
                    int sendByteLength = 0;
                    byte[] packet =new CreateProtocol(HeaderStartFlag.DATA, HeaderId.DATA_START,file.getName().getBytes()).toProtocol();
                    System.out.println("name packet length = "+packet.length + "      file name length = " + file.getName().length());
                    for(int i = 0 ; i < 9 ; i ++){
                        System.out.print(i + " = " +packet[i] +"\t");
                    }
                    System.out.println();
                    mOutputStream.write(packet);
                    mOutputStream.flush();

                    byte[] fileByte = Files.readAllBytes(file.toPath());
                    packet =new CreateProtocol(HeaderStartFlag.DATA,HeaderId.DATA_BODY,fileByte).toProtocol();
                    for(int i = 0 ; i < 9 ; i ++){
                        System.out.print(i + " = " +packet[i] +"\t");
                    }
                    System.out.println();
                    mOutputStream.write(packet);
                    mOutputStream.flush();

                    packet = new CreateProtocol(HeaderStartFlag.DATA, HeaderId.DATA_END,null).toProtocol();
                    for(int i = 0 ; i < 5 ; i ++){
                        System.out.print(i + " = " +packet[i] +"\t");
                    }
                    System.out.println();
                    mOutputStream.write(packet);
                    mOutputStream.flush();

                    System.out.println("sendFile TOTAL - " + file.length());
                }else{
                    System.out.println("mode miss");
                }

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
        int length;
        while(true){
            try{
                length = mInputStream.available();
                if(length >= AnalysisProtocolHeader.BASIC_HEADER_LENGTH ) {
                    byte[] headerPacket = new byte[AnalysisProtocolHeader.BASIC_HEADER_LENGTH ];
                    mInputStream.read(headerPacket);
                    AnalysisProtocolHeader header = new AnalysisProtocolHeader(headerPacket);
                    System.out.println("Header Start flag = " + header.getHeaderStartFlag());
                    System.out.println("Header Id flag = " + header.getHeaderId());
                    System.out.println("Header End flag ( 예비바이트) = " + header.getHeaderEndFlag());
                    System.out.println("check order = " + header.getHeaderOrderTable());
                    System.out.println("Header length = " + header.getDataLength());
                    System.out.println("Header length check = " + header.getDataLength());
                    if(header.getDataLength() !=0){
                        while(mInputStream.available() < AnalysisProtocolHeader.CRC);
                        byte[] crc = new byte[AnalysisProtocolHeader.CRC];
                        mInputStream.read(crc);
                        header.addCRC(crc);
                        while(mInputStream.available() < header.getDataLength());
                        byte[] data = new byte[header.getDataLength()];
                        mInputStream.read(data);
                        header.addCRC(data);

                        System.out.println("Header crc = " + header.getCrc());
                        System.out.println("Header data = " + header.getData());
                    }else{
                        System.out.println("data 미존재");
                    }
                }else{
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                try {
                    mInputStream.close();
                } catch (IOException e1) {
                }
            }
        }
    }
}