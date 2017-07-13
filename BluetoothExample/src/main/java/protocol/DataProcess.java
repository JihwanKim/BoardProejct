package protocol;

import java.io.*;
import java.net.Socket;
import java.util.Hashtable;

/**
 * Created by jihwa on 2017-05-24.
 */

public class DataProcess {
    private static final Hashtable<HeaderId,IDataProcess> processHash = new Hashtable<HeaderId,IDataProcess>();

    private static FileOutputStream fileOutputStream = null;
    private static File mFile = null;

    private final HeaderStartFlag mHeaderStartFlag;
    private final HeaderEndFlag mHeaderEndFlag;
    private final HeaderId mHeaderId;

    private static String path;

    private static InputStream bluetoothInputStream = null;
    private static OutputStream bluetoothOutputStream = null;

    public static void setPath(String path){
        DataProcess.path = path;
    }

    public static void setStream(Socket socket){
        if(socket == null)
            return;
        try {
            bluetoothInputStream = socket.getInputStream();
            bluetoothOutputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 실질적으로 실행할 함수를 식별할 때 사용하는 값은 HeaderId 하나만 사용하며, 다른 값은
    // 이후 추가될 수 있으므로 남겨놓음.
    public DataProcess(HeaderId headerId){
        this(null,null, headerId);
    }

    public DataProcess(HeaderStartFlag headerStartFlag, HeaderEndFlag headerEndFlag, HeaderId headerId) {
        this.mHeaderStartFlag = headerStartFlag;
        this.mHeaderEndFlag = headerEndFlag;
        this.mHeaderId = headerId;
        initHashtable();
    }

    // param data : 처리할 데이터를 입력.
    // 해당 클래스를 생성할 때 사용했던
    public void process(byte[] data){
        processHash.get(mHeaderId).process(data);
    }


    // status 와 data만 구현
    // 각 해쉬테이블에 key 값은 HeaderId enum 로, value는 IDataProcess Interface 를 사용한다.
    // 각  value에는 해당 Id가 들어왔을 때 동작하는 것을 나타낸다.
    private void initHashtable(){
        if(processHash.size() <1){
            // status part
        }
    }

    // 처리할 method들을 추가하는 부분.
    // 해당 method를 통해 단 한번만 추가하면됨.
    public DataProcess addProcess(HeaderId headerId, IDataProcess dataProcess){
        processHash.put(headerId,dataProcess);
        return this;
    }

    // 위 method의 static version.
    public static void addStaticProcess(HeaderId headerId, IDataProcess dataProcess){
        processHash.put(headerId,dataProcess);
    }
}
