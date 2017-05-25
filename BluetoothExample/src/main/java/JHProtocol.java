import protocol.EndFlag;
import protocol.Id;
import protocol.StartFlag;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * Created by jihwa on 2017-05-19.
 * start flag = 0xFA 로 동일
 * test : 0xFA /0x00 0x01 /0x02 /0x01 0xFF/0x00
 * orderName    Start flag = 1byte       Length = 2byte      ID = 1byte      Data bytes = Length       End StartFlag = 1byte
 *  MODULE_CONTROL        0xFA                                            0x02            0x00 0x00               0xFA[Q]
 *
 *  DATA_SAVE       0xFA                                            0x20            0x00 0x00                   0xFA[R]
 *  DATA_SAVE_Er    0xFA                                            0x20            0x00 0x01                   0xFA[R]
 */
public class JHProtocol {
    private JHProtocol() {
    }
    // 단위 byte
    private static final int START_FLAG = 1;
    private static final int END_FLAG = 1;
    private static final int ID = 1;
    private static final int LENGTH = 2;
    public static final int HEADER_LENGTH = START_FLAG + END_FLAG + ID + LENGTH ;

    // 헤더생성
    // exception case : 잘못된 명령
    // 1 1 1 2
    public static byte[] makeHeader(StartFlag startFlag, Id id) {
        byte[] bytes = new byte[HEADER_LENGTH];
        bytes[0] = StartFlag.getByte(startFlag);
        bytes[1] = EndFlag.getByteUsingStartFlag(startFlag);
        bytes[2] = Id.getByte(id);
        //
        return bytes;
    }

    @SuppressWarnings("Since15")
    public static byte[] getHeader(byte[] bytes){
        return Arrays.copyOfRange(bytes,0, HEADER_LENGTH);
    }

    public static byte[] makePacket(byte[] header,byte[] data){
        // dataLength
        if(data != null && data.length > 0) {
            byte[] dataLength = BigInteger.valueOf(data.length).toByteArray();
            System.out.println("datalength = "+dataLength.length + "  real length = " + data.length );
            for(int i = 0 ; i < dataLength.length ; i++){
                System.out.println(i+ " + " + dataLength[i]);
            }
            if(dataLength.length != 1)
                header[3] = dataLength[1];
            if(dataLength[0] == 0 && dataLength.length > 2)
                header[4] = dataLength[2];
            else
                header[4] = dataLength[0];
            return arrayCombine(header, data);
        }
        header[3] = 0x00;
        header[4] = 0x00;
        return header;
    }

    // 헤더 및 데이터 분석
    // 0xFF : 잘못된 명령으로 전송되었을 경우.
    public static StartFlag getStartFlag(byte[] bytes)  {
        return StartFlag.getStartFlag(bytes[0]);
    }


    public static EndFlag getEndFlag(byte[] bytes)  {
        return EndFlag.getEndFlag(bytes[1]);
    }

    public static Id getId(byte[] bytes)  {
        return Id.getId(bytes[2]);
    }

    public static int getLength(byte[] bytes)  {
        int a = (bytes[3]&0xFF)<<8;
        int b = bytes[4];
        //(((int)bytes[1]&0xFF)<<8 )+ (int)bytes[0]&0xFF; ????<< 왜 얘는 안되지 ?
        int result = a+b;
        // 왼쪽꺼가 연산이 안됨 .. ? ?
        System.out.println(((bytes[1]&0xFF)<<8 )+ (int)bytes[0]&0xFF);
        return result;
    }

    @SuppressWarnings("Since15")
    public static byte[] getData(byte[] bytes,int length)  {
        return Arrays.copyOfRange(bytes,5,length);
    }


    public static byte[] arrayCombine(byte[] srcF,byte[]srcS){
        byte[] bytes = new byte[srcF.length+srcS.length];
        System.arraycopy(srcF,0,bytes,0,srcF.length);
        System.arraycopy(srcS,0,bytes,srcF.length,srcS.length);

        return bytes;
    }

    // 명령어 . Start > ?
    // 명령어 .
    // control start
    // status power
    // data save > 저장할 데이터를 보내라.
    //
    public static byte[] convertToPacket(String str){
        byte packet[]= null;
        byte header[] = null;
        byte data[] = null;
        String[] order = str.split(" ");
        String thirdOrder = null;

        StartFlag flag = StartFlag.getStartFlag(order[0]);
        Id id = Id.getId(order[1]);
        if(order.length>2){
            thirdOrder = order[2];
            int time = 0;
            try{
                time = Integer.parseInt(thirdOrder);
            }catch(Exception e){

            }

            if(time ==0){
                File file = new File(thirdOrder);
                try {
                    FileInputStream oInputStream = new FileInputStream(file);
                    int nCount = oInputStream.available();
                    if (nCount > 0) {
                        data = new byte[nCount];
                        oInputStream.read(data);
                    }

                    if (oInputStream != null) {
                        oInputStream.close();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                data = BigInteger.valueOf(time).toByteArray();
            }
        }
        header = makeHeader(flag, id);

        packet = makePacket(header, data);

        return packet;
    }
}
