import com.sun.istack.internal.NotNull;

import java.io.*;
import java.util.Date;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

public class ObexPutClient {

    public static void main(String[] args) throws IOException, InterruptedException {

        String serverURL = null; // = "btgoep://0019639C4007:6";
        if ((args != null) && (args.length > 0)) {
            serverURL = args[0];
        }
        if (serverURL == null) {
            String[] searchArgs = null;
            // Connect to OBEXPutServer from examples
             searchArgs = new String[] { "0000110100001000800000805F9B34FB" };
            ServicesSearch.main(searchArgs);
            if (ServicesSearch.serviceFound.size() == 0) {
                System.out.println("OBEX service not found");
                return;
            }
            // Select the first service found
            serverURL = (String)ServicesSearch.serviceFound.elementAt(0);
        }
        //String value = "btspp://F8E61A466934:5;authenticate=false;encrypt=false;master=false";
        System.out.println("connect url for spp"+serverURL);
        // 참고링크
        // http://stackoverflow.com/questions/15343369/sending-a-string-via-bluetooth-from-a-pc-as-client-to-a-mobile-as-server
        final StreamConnection streamConnection = (StreamConnection) Connector.open(serverURL);
        InputStream inputStream = streamConnection.openInputStream();
        OutputStream outputStream = streamConnection.openOutputStream();
        Thread recvMsg = new Thread(new ReceiveThread(inputStream));
        Thread sendMsg = new Thread(new SendThread(outputStream));

        sendMsg.start();
        recvMsg.run();
        streamConnection.close();
    }

}

class SendThread implements Runnable{
    final OutputStream mOutputStream;
    public SendThread(@NotNull OutputStream stream) {
        mOutputStream = stream;
    }
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(mOutputStream));
        while(true){
            try {
                System.out.print("보낼 메세지 : ");
                String sendMsg = reader.readLine();
                printWriter.write(sendMsg + "\n");
                printWriter.flush();

                System.out.println("["+new Date()+"]" + sendMsg);
            } catch (IOException e) {
                try {
                    mOutputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
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
                    e.printStackTrace();
                }
            }
        }
    }
}