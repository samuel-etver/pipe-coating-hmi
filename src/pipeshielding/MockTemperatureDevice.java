package pipeshielding;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MockTemperatureDevice {
  private static final float[] BASICS = {
    10,
    20,
    30,
    40,
    50,
    
    60,
    70,
    80,
    90
  };
  
  private final Thread mSendThread;
  private final Thread mUpdateThread;
  private volatile boolean mTerminate;
  private final double[] mValues = new double[TemperatureDeviceProperties.VALUES_COUNT];
  
  
  public MockTemperatureDevice() {            
    mUpdateThread = new Thread(new Runnable() {
      public void run() {
        runUpdating();  
      }
    });

    mSendThread = new Thread(new Runnable() {
      public void run() {
        runSending();
      }
    });
  }
  
  
  public void start() {
    if(Global.USE_MOCK_DEVICES) {  
      mUpdateThread.start();
      mSendThread.start();
    }
  }
  
  
  public void terminate() {
    mTerminate = true;
  }
  
  
  public void join() {
    try {
      mUpdateThread.join();
    }  
    catch(Exception ex) {        
    }
    
    try {        
      mSendThread.join();
    }
    catch(Exception ex) {        
    }
  }
  
  
  private void runUpdating() {
    while(!mTerminate) {         
      updateValues();  
      try {
        Thread.sleep(100);  
      }  
      catch(Exception ex) {          
      }
    }
  }
  
  
  private synchronized void updateValues() {     
    for(int i = 0; i < mValues.length; i++) {
      mValues[i] = BASICS[i] + 3*(0.5 - Math.random());
    }
  }
  
  
  private void runSending() {
    final byte[] buffer = new byte[TemperatureDeviceProperties.PACKET_SIZE];
    initPacket(buffer);
    
    DatagramSocket socket = null;  
    InetAddress inetAddress;
    
    communication: {
      try {  
        inetAddress = InetAddress.getLocalHost();
        socket = new DatagramSocket();
      }
      catch(Exception ex) {        
        ex.printStackTrace();
        break communication;
      }
    
      while(!mTerminate) {
        try {
          Thread.sleep(200);
        }
        catch(Exception ex) {
          break communication;
        }              
        
        buildPacket(buffer);
        
        final DatagramPacket packet =
          new DatagramPacket(buffer, buffer.length, inetAddress, Global.temperatureDevicePort);
        try {
          socket.send(packet);
        }
        catch(Exception ex) {            
          ex.printStackTrace();
        }      
      }
    }
    
    if (socket != null) {
      socket.close();        
    }
  }
  
  
  private void initPacket(byte[] buffer) {     
    for(int i = 0; i < buffer.length; i++) {
      buffer[i] = 0;  
    }
    setI16(buffer,
            TemperatureDeviceProperties.OFFSET_DEVICE_ID,
            TemperatureDeviceProperties.DEVICE_ID);
  }
  
  
  private synchronized void buildPacket(byte[] buffer) {   
    for(int i = 0; i < mValues.length; i++) {
      setI16(buffer, 
              TemperatureDeviceProperties.OFFSET_VALUE_TX[i],
              valueToInt(mValues[i]));  
    }
  }
  
  
  private static void setI16(byte[] buffer, int offset, int value) {
    buffer[offset]     = (byte)(0xFF & value);
    buffer[offset + 1] = (byte)(0xFF & (value >> 8));
  }
  
  
  private static int valueToInt(double value) {
    return (int)(10.0*value);  
  }
}
