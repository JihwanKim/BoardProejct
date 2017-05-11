import javax.bluetooth.*;
import java.io.IOException;
import java.util.Vector;

/**
 * Created by jihwan on 2017-05-08.
 */
public class RemoteDeviceDiscovery {
    public static final Vector<RemoteDevice> devicesDiscovered = new Vector<RemoteDevice>();

    public static void main(String[] args) throws BluetoothStateException, InterruptedException {
        final Object inquiryCompleteEvent = new Object();
        devicesDiscovered.clear();

        DiscoveryListener listener = new DiscoveryListener() {
            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                System.out.println("Device " + btDevice.getBluetoothAddress() + " found");
                devicesDiscovered.addElement(btDevice);
                try{
                    System.out.println("name " + btDevice.getFriendlyName(false));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {

            }

            public void serviceSearchCompleted(int transID, int respCode) {

            }

            public void inquiryCompleted(int discType) {
                System.out.println("Device Inquiry completed");
                synchronized (inquiryCompleteEvent){
                    inquiryCompleteEvent.notifyAll();
                }
            }
        };
        synchronized (inquiryCompleteEvent){
            boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC,listener);
            if(started){
                System.out.println("wait for device inquiry to complete");
                inquiryCompleteEvent.wait();
                System.out.println(devicesDiscovered.size() + " device(s) found");
            }
        }
    }
}
