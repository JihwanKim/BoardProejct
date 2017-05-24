package com.example.jihwa.androidbluetoothwithbluecoveprac.protocol;


import android.os.Environment;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

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
    private final byte[] mData;
    private static final String TAG = "BluetoothServer";


    public DataProcess(@NotNull Id id){
        this(null,null,id,null);
    }

    public DataProcess(@NotNull Id id,byte[] data){
        this(null,null,id,data);
    }


    public DataProcess(StartFlag startFlag, EndFlag endFlag, @NotNull Id id, byte[] data) {
        this.mStartFlag = startFlag;
        this.mEndFlag = endFlag;
        this.mId = id;
        this.mData = data;
        initHashtable();
    }

    public IDataProcess getProcess(){
        return processHash.get(mId);
    }


    // status 와 data만 구현
    private void initHashtable(){
        if(processHash.size() <1){
            // status part
            processHash.put(Id.STATUS_POWER,(byte[] bytes) ->{

                });

            // data part
            processHash.put(Id.DATA_NAME, (byte[] data)->{
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TestPath";
                    mFile = new File(path);
                    mFile.mkdirs();
                    mFile = new File(path + "/" + new String(data) + new Date() + ".txt");
                    try {
                        fileOutputStream = new FileOutputStream(mFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "file write[" + mFile.getPath() + "] StartFlag[" + mFile.getName() + "] fileSize = " + mFile.length());
                });

            processHash.put(Id.DATA_BODY, (byte[] data)-> {
                try {
                    Log.d(TAG,"DATA BODY - data length = " + data.length);
                    fileOutputStream.write(data);
                    fileOutputStream.flush();
                } catch (IOException e) {
                    Log.d(TAG,"file output exception in process" + e);
                }
            });


            processHash.put(Id.DATA_END, (byte[] data)-> {
                try {
                    Log.d(TAG,"DATA END - file length = " + mFile.length());
                    fileOutputStream.close();
                    fileOutputStream = null;
                    mFile = null;
                } catch (IOException e) {
                    Log.d(TAG,"file output exception in process" + e);
                }
            });

        }
    }
}
