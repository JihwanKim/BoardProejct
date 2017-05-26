package com.example.jihwa.androidbluetoothwithbluecoveprac.protocol;


import android.os.Environment;

import com.example.jihwa.androidbluetoothwithbluecoveprac.SocketManager;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Queue;

/**
 * Created by jihwa on 2017-05-24.
 */

public class DataProcess {
    private static final Hashtable<Id,IDataProcess> processHash = new Hashtable<>();

    private static FileOutputStream fileOutputStream = null;
    private static File mFile = null;

    private final StartFlag mStartFlag;
    private final EndFlag mEndFlag;
    private final Id mId;
    private static final String TAG = "BluetoothServer";


    // setting part
    // computer 환경이라면, 모든 Log부분에 주석을 달아주세요.

    // 실행 환경에 맞게끔, usePath를 변경.
    private static final String androidPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TestPath";
    private static final String computerPATH = "";

    private static final String usePath = androidPATH;
    private static final Queue<String> mMsgQueue = SocketManager.getmMsgQueue();

    // 실질적으로 실행할 함수를 식별할 때 사용하는 값은 Id 하나만 사용하며, 다른 값은
    // 이후 추가될 수 있으므로 남겨놓음.
    public DataProcess(@NotNull Id id){
        this(null,null,id);
    }

    public DataProcess(StartFlag startFlag, EndFlag endFlag, @NotNull Id id) {
        this.mStartFlag = startFlag;
        this.mEndFlag = endFlag;
        this.mId = id;
        initHashtable();
    }

    // param data : 처리할 데이터를 입력.
    // 해당 클래스를 생성할 때 사용했던
    public void process(byte[] data){
        processHash.get(mId).process(data);
    }


    // status 와 data만 구현
    // 각 해쉬테이블에 key 값은 Id enum 로, value는 IDataProcess Interface 를 사용한다.
    // 각  value에는 해당 Id가 들어왔을 때 동작하는 것을 나타낸다.
    private void initHashtable(){
        if(processHash.size() <1){
            // status part
            processHash.put(Id.STATUS_COIN_BATTERY,(byte[] bytes) ->{
                mMsgQueue.add("BATTERY STATUS : "+new String(bytes));
            });

            // data part
            processHash.put(Id.DATA_NAME, (byte[] data)->{
                    String path = usePath;
                    mFile = new File(path);
                    mFile.mkdirs();
                    mFile = new File(path + "/" + new String(data) + new Date() + ".txt");
                    try {
                        fileOutputStream = new FileOutputStream(mFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    final String logString =  "file write[" + mFile.getPath() + "] StartFlag[" + mFile.getName() + "] fileSize = " + mFile.length();
                    Logging.log(logString);
                });

            processHash.put(Id.DATA_BODY, (byte[] data)-> {
                try {
                    final String logString = "DATA BODY - data length = " + data.length;
                    Logging.log(logString);
                    if(data!=null)
                        fileOutputStream.write(data);
                    fileOutputStream.flush();
                } catch (IOException e) {
                    Logging.log("file output exception in process" + e);
                }
            });


            processHash.put(Id.DATA_END, (byte[] data)-> {
                try {
                    final String logString = "DATA END - file length = " + mFile.length();
                    Logging.log(logString);
                    fileOutputStream.close();
                    fileOutputStream = null;
                    mFile = null;
                } catch (IOException e) {
                    Logging.log("file output exception in process" + e);
                }
            });
        }
    }
}
