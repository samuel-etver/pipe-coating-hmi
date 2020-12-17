package pipeshielding;

public class TemperatureDeviceProperties {
  public static final int DEVICE_ID = 0x73e4;
  public static final int PACKET_SIZE = 40;  
  
  public static final int VALUES_COUNT = 9;
  
  public static final int OFFSET_DEVICE_ID = 0;  
  public static final int[] OFFSET_VALUE_TX = {
    2,
    4,
    6,
    8,
    10,
    
    12,
    14,
    16,
    18
  };

  public final static String[] TEMPERATURE_CAPTIONS = {
    "Температура трубы перед индуктором",
    "Температура трубы после индуктора",
    "Температура трубы перед эпоксидной камерой",
    "Температура трубы перед нанесением адгезива",
    "Температура трубы перед охлаждением",
    "Температура трубы в камере охлаждения",
    "Температура трубы после охлаждения",
    "Температура пленки адгезива",
    "Температура пленки полиэтилена"
  };
    
  private TemperatureDeviceProperties() {      
  }    
}
