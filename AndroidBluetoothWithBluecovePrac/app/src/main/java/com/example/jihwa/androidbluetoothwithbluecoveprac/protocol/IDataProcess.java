package com.example.jihwa.androidbluetoothwithbluecoveprac.protocol;

/**
 * Created by jihwa on 2017-05-24.
 */

@FunctionalInterface
public interface IDataProcess {
    void process(byte[] data);
}
