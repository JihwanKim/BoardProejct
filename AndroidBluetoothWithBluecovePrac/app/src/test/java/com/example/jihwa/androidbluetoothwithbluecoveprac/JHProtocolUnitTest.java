package com.example.jihwa.androidbluetoothwithbluecoveprac;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by jihwa on 2017-05-19.
 */

public class JHProtocolUnitTest {
//    @Test
//    //0xFA /0x00 0x01 /0x02 /0x01 0xFF/0x00
//    public void makeHeader_correct() throws Exception {
//        byte[] excepted = new byte[]{(byte) 0xFA,0x00,0x01,0x02,0x01, (byte) 0xFF,0x00};
//        byte[] data = new byte[]{0x01, (byte) 0xFF};
//        byte[] actual = JHProtocol.makeHeader(JHProtocol.Flag.MODULE_CONTROL,1, JHProtocol.Id.POWER_ON,data, JHProtocol.EndFlag.WRITE);
//        assertEquals(new String(excepted).toString(), new String(actual).toString());
//    }
    @Test
    public void getLength_correct() throws Exception{
        int bytes = JHProtocol.getLength(new byte[]{0x01,0x01});
        assertEquals(0x0101,bytes);
    }
}