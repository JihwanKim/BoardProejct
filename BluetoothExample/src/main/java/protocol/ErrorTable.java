package protocol;

/**
 * Created by USER on 2017-06-19.
 */
public enum ErrorTable {
    ORDER_MISS,LENGTH_MISS,CRC_MISS,DATA_MISS, NO_ERROR,GET_CRC, START_FLAG_MISS, ID_MISS, END_FLAG_MISS, DATA_LENGTH, CRC_LENGTH;
    public boolean isError(){
        return this == GET_CRC;
    }
}
