package pipecoating;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Date;

class TemperatureDeviceReader {
  private final Thread mReadThread;
  private final Thread mWatchdogThread;
  private volatile Date mWatchdogDate;
  private DatagramSocket mSocket;
  private volatile boolean mTerminate;
  private final Float[] mValues = new Float[TemperatureDeviceProperties.VALUES_COUNT];
  
  
  public TemperatureDeviceReader() {
    mReadThread = new Thread(new Runnable() {
      public void run() {
        runReading();  
      }
    });
    
    mWatchdogThread = new Thread(new Runnable() {
      public void run() {
        runWatchdog();  
      }
    });
  }


  public void start() {
    mWatchdogDate = new Date();
    
    try {
      mSocket = new DatagramSocket(Global.temperatureDevicePort);
      mReadThread.start();
    }
    catch(SocketException exception) {
    }

    mWatchdogThread.start();
  }
  
  
  public void terminate() {  
    mTerminate = true; 
    try {
      mSocket.close();
    }
    catch(Exception ex) {        
    }
  }
  
  
  public void join() {
    try {
      mWatchdogThread.join();
    }  
    catch(Exception ex) {        
    }
    
    try {        
      mReadThread.join();
    }
    catch(Exception ex) {        
    }
  }
  
  
  private synchronized void resetWatchdog() {
    mWatchdogDate = new Date();  
  }


  private static int makeI16(byte lo, byte hi) {
    return (((int)lo) & 0xFF) | ((((int)hi) & 0xFF) << 8);
  }
  
  
  private static float intToFloat(int value) {
    return 0.1f*value; 
  }
  
  
  private boolean checkPacket(DatagramPacket packet) {      
    if(packet.getLength() != TemperatureDeviceProperties.PACKET_SIZE) {
      return false;    
    }
    
    final byte[] data = packet.getData();
    
    final int deviceId = makeI16(
            data[TemperatureDeviceProperties.OFFSET_DEVICE_ID],
            data[TemperatureDeviceProperties.OFFSET_DEVICE_ID + 1]);
    return deviceId == TemperatureDeviceProperties.DEVICE_ID;
  }


  private synchronized void read(DatagramPacket packet) {
    final byte[] data = packet.getData();
    
    for(int i = 0; i < mValues.length; i++) {
      final int offset = TemperatureDeviceProperties.OFFSET_VALUE_TX[i];  
      mValues[i] = intToFloat(makeI16(data[offset], data[offset + 1]));
    }
  }
  
  
  public synchronized DeviceData getDeviceData() {
    final DeviceData deviceData = new DeviceData();
    for(int i = 0; i < mValues.length; i++) {
      deviceData.mValues[i] = mValues[i];  
    }
    return deviceData;
  }
  

  private void runReading() {
    final byte[] buffer = new byte[TemperatureDeviceProperties.PACKET_SIZE];
    final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
    
    while(!mTerminate) {
      try {
        Thread.sleep(3);
      }
      catch(InterruptedException exception) {
        break;  
      }

      try {
        mSocket.receive(packet);
        if(checkPacket(packet)) {
          read(packet);          
          resetWatchdog();
        }
      }
      catch(Exception ex) {
      }
    }
  }
  
  
  private void runWatchdog() {
    while(!mTerminate) {  
      try {
        Thread.sleep(100);
      }   
      catch(Exception ex) {          
      }
      
      if (isWatchdogTimeExpired()) {
        resetValuesByWatchdog();
      }      
    }
  }
  
  
  private boolean isWatchdogTimeExpired() {
    return 0.001*(new Date().getTime() - mWatchdogDate.getTime()) > 1.0;
  }
  
  
  private synchronized void resetValuesByWatchdog() {
    for(int i = 0; i < mValues.length; i++) {
        mValues[i] = null;
    }
  }
  
  
  public static class DeviceData {
    private final Float[] mValues = new Float[TemperatureDeviceProperties.VALUES_COUNT];

    
    public Float getValue(int i) {
      return mValues[i];
    }
  }
}