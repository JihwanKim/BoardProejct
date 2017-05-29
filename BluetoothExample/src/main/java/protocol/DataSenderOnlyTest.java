package protocol;

import java.io.BufferedInputStream;
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
    public void sendData(String path){
        try {
            File file = new File(path);
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            byte[] fileBytes = new byte[(int) file.length()];
            buf.read(fileBytes);
            buf.close();
            //fileBytes = AnalysisProtocolHeader.arrayCombine(new CreateProtocolHeader(StartFlag.DATA,EndFlag.WRITE,Id.DATA_BODY,fileBytes.length).toHeader(),fileBytes);
            mOutputStream.write(fileBytes);
            mOutputStream.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
