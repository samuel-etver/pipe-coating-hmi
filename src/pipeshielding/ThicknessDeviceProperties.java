package pipeshielding;

public class ThicknessDeviceProperties {
  public static final int DEVICE_ID = 0x015f;
  public static final int PACKET_SIZE = 20;
  
  public static final int OFFSET_DEVICE_ID = 0;  
  public static final int OFFSET_VALUE_A1 = 2;
  public static final int OFFSET_VALUE_B1 = 4;
  public static final int OFFSET_VALUE_B2 = 6;
  public static final int OFFSET_VALUE_B3 = 8;
  public static final int OFFSET_VALUE_B4 = 10;
  
  public static final int VALUES_A_COUNT = 1;
  public static final int VALUES_B_COUNT = 4;
  
  private ThicknessDeviceProperties() {      
  }
}
