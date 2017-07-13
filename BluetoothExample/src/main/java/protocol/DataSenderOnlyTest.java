package protocol;


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

            mOutputStream.write(new CreateProtocol(HeaderStartFlag.DATA, HeaderEndFlag.WRITE, HeaderId.DATA_START,file.getName().getBytes()).toProtocol());
            mOutputStream.flush();


            FileInputStream inputStream = new FileInputStream(file);
            byte[] fileBytes = new byte[(int) file.length()];
            inputStream.read(fileBytes);
            inputStream.close();
            fileBytes = new CreateProtocol(HeaderStartFlag.DATA, HeaderEndFlag.WRITE, HeaderId.DATA_BODY,fileBytes).toProtocol();
            mOutputStream.write(fileBytes);
            mOutputStream.flush();


            mOutputStream.write(new CreateProtocol(HeaderStartFlag.DATA, HeaderEndFlag.WRITE, HeaderId.DATA_END,null).toProtocol());
            mOutputStream.flush();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }
}
