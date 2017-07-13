package protocol;

/**
 * Created by jihwa on 2017-05-24.
 */

public interface IDataProcess {
    // return log data
    Object process(byte[] data);
}
