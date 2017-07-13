package protocol;


import java.util.Arrays;

/**
 * Created by jihwa on 2017-05-23.
 */

public class AnalysisProtocolHeader {

    public static final int START_FLAG = 1;
    public static final int ID = 1;
    public static final int END_FLAG = 1;
    public static final int ORDER_TABLE = 1;
    public static final int LENGTH = 2;
    public static final int CHECK_LENGTH = 2;
    public static final int CRC = 2;

    public static final int BASIC_HEADER_LENGTH = START_FLAG + ID + END_FLAG + ORDER_TABLE + LENGTH + CHECK_LENGTH;
    public static final int HAS_DATA_HEADER_LENGTH = BASIC_HEADER_LENGTH + CRC;

    byte[] mPacket = null;
    private HeaderStartFlag headerStartFlag = null;
    private HeaderEndFlag headerEndFlag = null;
    private HeaderId headerId = null;
    private HeaderOrderTable headerOrderTable = null;
    private int crc = 0;
    public boolean hasCRC = false;

    private int dataLength = 0;

    public AnalysisProtocolHeader(byte[] packet) {
        mPacket = packet;
        analysisHeader();

    }

    // 헤더의 startFlag를 반환
    public HeaderStartFlag getHeaderStartFlag() {
        return headerStartFlag;
    }

    // 헤더의 endflag를 반환
    public HeaderEndFlag getHeaderEndFlag() {
        return headerEndFlag;
    }

    // 헤더의 ID를 반환
    public HeaderId getHeaderId() {
        return headerId;
    }

    // 헤더의 오더테이블을 반환
    public HeaderOrderTable getHeaderOrderTable() {
        return headerOrderTable;
    }

    // 헤더의 데이터 길이를 반환
    public int getDataLength() {
        return dataLength;
    }

    // 헤더의 crc값을 반환
    public int getCrc() {
        return crc;
    }

    // 해당 method 는 패킷으로 데이터를 읽었을때 사용. 근데 읽을일 없을꺼임.
    public byte[] getData() {
        return Arrays.copyOfRange(mPacket, 10, dataLength);
    }

    public ErrorTable analysisHeader() {
        headerStartFlag = HeaderStartFlag.getStartFlag(mPacket[0]);
        if (headerStartFlag == HeaderStartFlag.ERROR)
            return ErrorTable.START_FLAG_MISS;
        headerId = HeaderId.getId(mPacket[1]);
        if (headerId == HeaderId.ERROR)
            return ErrorTable.ID_MISS;

        headerEndFlag = HeaderEndFlag.getEndFlag(mPacket[2]);
        if (headerEndFlag == HeaderEndFlag.ERROR)
            return ErrorTable.END_FLAG_MISS;

        headerOrderTable = HeaderOrderTable.getOrder(mPacket[3]);
        if (headerOrderTable != HeaderOrderTable.makeOrder(headerStartFlag, headerEndFlag, headerId))
            return ErrorTable.ORDER_MISS;

        // data length
        int msb = (mPacket[4] & 0xFF) << 8;
        int lsb = mPacket[5] & 0xFF;
        dataLength = msb + lsb;

        // check data length
        msb = (mPacket[6] & 0xFF) << 8;
        lsb = mPacket[7] & 0xFF;
        if (dataLength != msb + lsb)
            return ErrorTable.LENGTH_MISS;
        if(dataLength > 0)
            hasCRC = true;

        return ErrorTable.NO_ERROR;
    }

    public void addCRC(byte[] crc) {
        if (dataLength != 0)
            // 8,9
            this.crc = ((crc[0]&0xFF) << 8) + (crc[1]&0xFF);

    }

    public boolean isHasCRC() {
        return hasCRC;
    }
}
