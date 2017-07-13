package protocol;

import java.math.BigInteger;


/**
 * Created by jihwa on 2017-05-25.
 */

public class CreateProtocol {
    private final HeaderStartFlag mHeaderStartFlag;
    private final HeaderEndFlag mHeaderEndFlag;
    private final HeaderId mHeaderId;
    private final HeaderOrderTable mHeaderOrderTable;
    private final int mLength;
    private byte []mData;


    //  위의 것은 EndFlag를 역할에 맞게 추론해서 자동으로 지정해주고, 아래것은 명시해주는것임.
    public CreateProtocol(HeaderStartFlag mHeaderStartFlag, HeaderId mHeaderId, byte[] data) {
        this(mHeaderStartFlag, HeaderEndFlag.getByteUsingStartFlag(mHeaderStartFlag), mHeaderId,data);
    }

    public CreateProtocol(HeaderStartFlag headerStartFlag, HeaderEndFlag headerEndFlag, HeaderId headerId, byte[] data) {
        this.mHeaderStartFlag = headerStartFlag;
        this.mHeaderEndFlag = headerEndFlag;
        this.mHeaderId = headerId;
        this.mHeaderOrderTable = HeaderOrderTable.makeOrder(mHeaderStartFlag,mHeaderEndFlag,mHeaderId);
        this.mData = data;
        if(mData != null)
            this.mLength = mData.length;
        else
            this.mLength = 0;
    }

    // 기존에 생성할때의 값들을 (header와 data의 array를 하나로 합쳐서) byte array로 만들어서 리턴함.
    public byte[] toProtocol(){
        byte[] protocol;
        if(mData!=null)
            protocol = new byte[10];
        else
            protocol = new byte[8];
        protocol[0] = mHeaderStartFlag.getByte();
        protocol[1] = mHeaderEndFlag.getByte();
        protocol[2] = mHeaderId.getByte();
        protocol[3] = mHeaderOrderTable.getByte();
        byte[] lengths = new BigInteger(String.valueOf(mLength)).toByteArray();
        if(lengths.length >1){
            protocol[4] = lengths[1];
        }else{
            protocol[5] = 0;
        }
        if(lengths.length>2)
            protocol[6] = lengths[2];
        else
            protocol[7] = lengths[0];

        // data check
        if(mData!=null) {
            int crc;
            crc = CRC16.getDataCRC(mData);
            protocol[8] = (byte) (crc >>> 8);
            protocol[9] = (byte) crc;
            protocol = arrayCombine(protocol, mData);
        }
        return protocol;
    }

    // srcF와 srcS 의 배열을 append 해서 결과값을 return한다.
    private byte[] arrayCombine(byte[] srcF,byte[]srcS){
        byte[] bytes = new byte[srcF.length+srcS.length];
        System.arraycopy(srcF,0,bytes,0,srcF.length);
        System.arraycopy(srcS,0,bytes,srcF.length,srcS.length);

        return bytes;
    }
}
