package protocol;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;

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
    //private static final String androidPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TestPath";
    private static final String computerPATH = "";

    private static final String usePath = computerPATH;


    public DataProcess( Id id){
        this(null,null,id);
    }



    public DataProcess(StartFlag startFlag, EndFlag endFlag,  Id id) {
        this.mStartFlag = startFlag;
        this.mEndFlag = endFlag;
        this.mId = id;
        initHashtable();
    }

    // @param data : 처리할 데이터를 입력.
    public void process(byte[] data){
        processHash.get(mId).process(data);
    }


    // status 와 data만 구현
    private void initHashtable(){
        if(processHash.size() <1){
            // status part
            processHash.put(Id.STATUS_COIN_BATTERY,(byte[] bytes) ->{
                });

            // data part
            processHash.put(Id.DATA_NAME, (byte[] data)->{
                    String path = usePath;
                    mFile = new File(path);
                    mFile.mkdirs();
                    mFile = new File(path + new String(data));
                    try {
                        fileOutputStream = new FileOutputStream(mFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    final String logString =  "file write[" + mFile.getPath() + "] StartFlag[" + mFile.getName() + "] fileSize = " + mFile.length();
                    //Log.d(TAG, logString);
                });

            processHash.put(Id.DATA_BODY, (byte[] data)-> {
                try {
                    final String logString = "DATA BODY - data length = " + data.length;
                    //Log.d(TAG,logString);
                    if(data!=null)
                        fileOutputStream.write(data);
                    fileOutputStream.flush();
                } catch (IOException e) {
                    //Log.d(TAG,"file output exception in process" + e);
                }
            });


            processHash.put(Id.DATA_END, (byte[] data)-> {
                try {
                    final String logString = "DATA END - file length = " + mFile.length();
                    //Log.d(TAG,logString);
                    fileOutputStream.close();
                    fileOutputStream = null;
                    mFile = null;
                } catch (IOException e) {
                    //Log.d(TAG,"file output exception in process" + e);
                }
            });
        }
    }
}
