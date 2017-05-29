package com.example.jihwa.androidbluetoothwithbluecoveprac.protocol;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by jihwa on 2017-05-25.
 */

public class DataSenderOnlyTest {
    private final OutputStream mOutputStream;

    public DataSenderOnlyTest(OutputStream socketOutputStream) {
        this.mOutputStream = socketOutputStream;
    }
    // path로 들어오는 파일을 불러들여서, 상대편으로 보내준다.
    public void sendData(String path){
        try {
            File file = new File(path);
            Log.d("bluetoothserver","file = "+ file.length()  + "\t filename " + file.getName() + "\t filepath = " + file.getAbsolutePath());

            mOutputStream.write(new CreateProtocol(StartFlag.DATA,EndFlag.WRITE,Id.DATA_NAME,file.getName().getBytes()).toProtocol());
            mOutputStream.flush();
            Log.d("bluetoothserver","file name flush ");


            FileInputStream inputStream = new FileInputStream(file);
            Log.d("bluetoothserver","inputstrea = ");
            byte[] fileBytes = new byte[(int) file.length()];
            Log.d("bluetoothserver","file = "+ fileBytes.length);
            inputStream.read(fileBytes);
            inputStream.close();
            fileBytes = new CreateProtocol(StartFlag.DATA,EndFlag.WRITE,Id.DATA_BODY,fileBytes).toProtocol();
            mOutputStream.write(fileBytes);
            mOutputStream.flush();
            Log.d("bluetoothserver","file = "+ fileBytes.length);


            mOutputStream.write(new CreateProtocol(StartFlag.DATA,EndFlag.WRITE,Id.DATA_END,null).toProtocol());
            mOutputStream.flush();
            Log.d("bluetoothserver","sendtest end = "+ fileBytes.length);
        } catch (FileNotFoundException e) {
            Log.d("bluetoothsever",e.getMessage());
        } catch (IOException e) {
            Log.d("bluetoothsever",e.getMessage());
        }
    }
}
