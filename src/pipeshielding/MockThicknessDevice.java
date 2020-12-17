package pipeshielding;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;

public final class MockThicknessDevice {
  private static final float SIGNALBREAK_PERIOD_ACTIVE1 = 1.5f;  
  private static final float SIGNALBREAK_PERIOD_ACTIVE2 = 6.0f;
  private static final float SIGNALBREAK_PERIOD_NOT_ACTIVE1 = 4.0f;
  private static final float SIGNALBREAK_PERIOD_NOT_ACTIVE2 = 9.0f;
  private static final float PERIOD_A1 = 2.0f;
  private static final float PERIOD_B1 = 3.2f;
  private static final float PERIOD_B2 = 4.1f;
  private static final float PERIOD_B3 = 5.3f;
  private static final float PERIOD_B4 = 7.1f;
  private static final float AMPLITUDE_A1 = 1.0f;
  private static final float AMPLITUDE_B1 = 1.5f;
  private static final float AMPLITUDE_B2 = 2.0f;
  private static final float AMPLITUDE_B3 = 2.5f;
  private static final float AMPLITUDE_B4 = 2.9f;
  
  private final Thread mSendThread;
  private final Thread mUpdateThread;
  private final Thread mSignalBreakThread;
  private volatile boolean mTerminate;
  private final PeriodValue mValueA1 = new PeriodValue();
  private final PeriodValue mValueB1 = new PeriodValue();
  private final PeriodValue mValueB2 = new PeriodValue();
  private final PeriodValue mValueB3 = new PeriodValue();
  private final PeriodValue mValueB4 = new PeriodValue();
  private volatile boolean mSignalBreak;
  
  private static class PeriodValue {
    volatile double value;
    double period;
    double time;
    double amplitude;
  }
  
  public MockThicknessDevice() {
    mValueA1.amplitude = AMPLITUDE_A1;
    mValueA1.period = PERIOD_A1;
    mValueB1.amplitude = AMPLITUDE_B1;
    mValueB1.period = PERIOD_B1;
    mValueB2.amplitude = AMPLITUDE_B2;
    mValueB2.period = PERIOD_B2;
    mValueB3.amplitude = AMPLITUDE_B3;
    mValueB3.period = PERIOD_B3;
    mValueB4.amplitude = AMPLITUDE_B4;
    mValueB4.period = PERIOD_B4;
            
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
    
    mSignalBreakThread = new Thread(new Runnable() {
      public void run() {
        runSignalBreak();  
      }
    });
  }
  
  
  public void start() {
    if(Global.USE_MOCK_DEVICES) {  
      mUpdateThread.start();
      mSendThread.start();
      mSignalBreakThread.start();
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
    
    try {
      mSignalBreakThread.join();
    }
    catch(Exception ex) {        
    }
  }
  
  
  private void runUpdating() {
    Date lastDate = new Date();  
    Date currDate;
    while(!mTerminate) {   
      try {
        Thread.sleep(50);  
      }  
      catch(Exception ex) {          
      }
      
      currDate = new Date();
      updateValues(0.001*(currDate.getTime() - lastDate.getTime()));  
      lastDate = currDate;
    }
  }
  
  
  private synchronized void updateValues(double timeDelta) {     
    final PeriodValue[] periodValues = {
      mValueA1,
      mValueB1,
      mValueB2,
      mValueB3,
      mValueB4
    };
    final double pi2 = 2*Math.PI;
    for(int i = 0; i < periodValues.length; i++) {
      final PeriodValue pv = periodValues[i];
      pv.time = (pv.time + timeDelta) % pv.period;
      pv.value = pv.amplitude*(1.0 + Math.sin(pi2*pv.time/pv.period));
    }
  }
  
  
  private void breakSignal() {
    mSignalBreak = true;
  }
  
  
  private void restoreSignal() {
    mSignalBreak = false;  
  }
  
  
  private boolean isSignalBroken() {
    return mSignalBreak;  
  }
  
  
  private void runSending() {
    final byte[] buffer = new byte[ThicknessDeviceProperties.PACKET_SIZE];
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
          Thread.sleep(25);
        }
        catch(Exception ex) {
          break communication;
        }              
        
        if(isSignalBroken()) {
          continue;    
        }
        
        buildPacket(buffer);
        
        final DatagramPacket packet =
          new DatagramPacket(buffer, buffer.length, inetAddress, Global.thicknessDevicePort);
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
            ThicknessDeviceProperties.OFFSET_DEVICE_ID,
            ThicknessDeviceProperties.DEVICE_ID);
  }
  
  
  private synchronized void buildPacket(byte[] buffer) {      
    setI16(buffer, 
            ThicknessDeviceProperties.OFFSET_VALUE_A1,
            valueToInt(mValueA1.value));  
    setI16(buffer,
            ThicknessDeviceProperties.OFFSET_VALUE_B1,
            valueToInt(mValueB1.value));
    setI16(buffer,
            ThicknessDeviceProperties.OFFSET_VALUE_B2,
            valueToInt(mValueB2.value));
    setI16(buffer,
            ThicknessDeviceProperties.OFFSET_VALUE_B3,
            valueToInt(mValueB3.value));
    setI16(buffer,
            ThicknessDeviceProperties.OFFSET_VALUE_B4,
            valueToInt(mValueB4.value));
  }
  
  
  private static void setI16(byte[] buffer, int offset, int value) {
    buffer[offset]     = (byte)(0xFF & value);
    buffer[offset + 1] = (byte)(0xFF & (value >> 8));
  }
  
  
  private static int valueToInt(double value) {
    return (int)(100.0*value);  
  }
  
  
  private void runSignalBreak() {
    final double[] periods = {
      SIGNALBREAK_PERIOD_NOT_ACTIVE1,
      SIGNALBREAK_PERIOD_ACTIVE1,
      SIGNALBREAK_PERIOD_NOT_ACTIVE2,
      SIGNALBREAK_PERIOD_ACTIVE2
    };
    
    int periodIndex = 0;
    Date lastDate = new Date();
    
    while(!mTerminate) {
      try {
        Thread.sleep(50);  
      }      
      catch(Exception ex) {
        break;  
      }
      
      final Date currDate = new Date();
      if(0.001*(currDate.getTime() - lastDate.getTime()) >= periods[periodIndex]) {
        if (isSignalBroken()) {
          restoreSignal();
        }
        else {
          breakSignal();
        }            
        if (++periodIndex == periods.length) {
          periodIndex = 0;
        }
        lastDate = currDate;
      }
    }
  }
}
