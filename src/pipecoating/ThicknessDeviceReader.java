package pipecoating;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Date;


class ThicknessDeviceReader {
  private final Thread mReadThread;
  private final Thread mWatchdogThread;
  private volatile Date mWatchdogDate;
  private DatagramSocket mSocket;
  private volatile boolean mTerminate;
  private volatile Float mA1Value;
  private volatile Float mB1Value;
  private volatile Float mB2Value;
  private volatile Float mB3Value;
  private volatile Float mB4Value;
  
  
  public ThicknessDeviceReader() {
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
      mSocket = new DatagramSocket(Global.thicknessDevicePort);
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
    return 0.01f*value; 
  }
  
  
  private boolean checkPacket(DatagramPacket packet) {      
    if(packet.getLength() != ThicknessDeviceProperties.PACKET_SIZE) {
      return false;    
    }
    
    final byte[] data = packet.getData();
    
    final int deviceId = makeI16(
            data[ThicknessDeviceProperties.OFFSET_DEVICE_ID],
            data[ThicknessDeviceProperties.OFFSET_DEVICE_ID + 1]);
    return deviceId == ThicknessDeviceProperties.DEVICE_ID;
  }


  private synchronized void read(DatagramPacket packet) {
    final byte[] data = packet.getData();

    mA1Value = intToFloat(makeI16(
            data[ThicknessDeviceProperties.OFFSET_VALUE_A1], 
            data[ThicknessDeviceProperties.OFFSET_VALUE_A1 + 1]));
    mB1Value = intToFloat(makeI16(
            data[ThicknessDeviceProperties.OFFSET_VALUE_B1],
            data[ThicknessDeviceProperties.OFFSET_VALUE_B1 + 1]));
    mB2Value = intToFloat(makeI16(
            data[ThicknessDeviceProperties.OFFSET_VALUE_B2],
            data[ThicknessDeviceProperties.OFFSET_VALUE_B2 + 1]));
    mB3Value = intToFloat(makeI16(
            data[ThicknessDeviceProperties.OFFSET_VALUE_B3],
            data[ThicknessDeviceProperties.OFFSET_VALUE_B3 + 1]));
    mB4Value = intToFloat(makeI16(
            data[ThicknessDeviceProperties.OFFSET_VALUE_B4],
            data[ThicknessDeviceProperties.OFFSET_VALUE_B4 + 1]));
  }
  
  
  public synchronized DeviceData getDeviceData() {
    final DeviceData deviceData = new DeviceData();
    deviceData.mA1Value = mA1Value;
    deviceData.mB1Value = mB1Value;
    deviceData.mB2Value = mB2Value;
    deviceData.mB3Value = mB3Value;
    deviceData.mB4Value = mB4Value;
    return deviceData;
  }
  

  private void runReading() {
    final byte[] buffer = new byte[ThicknessDeviceProperties.PACKET_SIZE];
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
    mA1Value = null;
    mB1Value = null;
    mB2Value = null;
    mB3Value = null;
    mB4Value = null;
  }
  
  
  public static class DeviceData {
    private Float mA1Value;
    private Float mB1Value;
    private Float mB2Value;
    private Float mB3Value;
    private Float mB4Value;

    
    public Float getA1Value() {
      return mA1Value;
    }
    
    
    public Float getB1Value() {
      return mB1Value;  
    }
    
    
    public Float getB2Value() {
      return mB2Value;  
    }
    
    
    public Float getB3Value() {
      return mB3Value;  
    }
    
    
    public Float getB4Value() {
      return mB4Value;  
    }
  }
}
