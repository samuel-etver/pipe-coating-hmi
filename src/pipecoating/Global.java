package pipecoating;

import java.awt.*;

public class Global {
  public static final boolean USE_MOCK_DEVICES = true;
  
  public static final String VERSION = "1.0.3";
  public static final String RELEASE_DATE = "2003-08-24";
  
  public static String CFG_FILE_NAME = "pipecoating.config";
  
  private static final String KEY_THICKNESS_DEVICE_PORT = "thicknessDevicePort";
  private static final String KEY_TEMPERATURE_DEVICE_PORT = "temperatureDevicePort";
  private static final String KEY_CHART_INDEX = "chartIndex";
  private static final String KEY_TEMPERATURE = "temperature";
  private static final String KEY_TEMPERATURE_FACTOR = "Factor";
  
  public static int thicknessDevicePort;
  public static int temperatureDevicePort;

  public static float[][] temperatureFactors = new float[TemperatureDeviceProperties.VALUES_COUNT][3];
  
  public static final int[] chartIndexes = new int[10];


  public static void load() {
    final ConfigFile cfg = new ConfigFile();

    cfg.load(CFG_FILE_NAME);

    thicknessDevicePort = cfg.readInteger(KEY_THICKNESS_DEVICE_PORT, 9999);
    temperatureDevicePort = cfg.readInteger(KEY_TEMPERATURE_DEVICE_PORT, 9998);

    for(int i=0; i<TemperatureDeviceProperties.VALUES_COUNT; i++) {
      for(int j=0; j<3; j++) {
        temperatureFactors[i][j] =
          cfg.readFloat(KEY_TEMPERATURE + i + KEY_TEMPERATURE_FACTOR + j, j == 1 ? 1.0f : 0.0f);
      }
    }
    
    for(int i=0; i <chartIndexes.length; i++) {
      chartIndexes[i] = cfg.readInteger(KEY_CHART_INDEX + Integer.toString(i), 0);
    }
  }


  public static void save() {
    final ConfigFile cfg = new ConfigFile();

    cfg.writeInteger(KEY_THICKNESS_DEVICE_PORT, thicknessDevicePort);
    cfg.writeInteger(KEY_TEMPERATURE_DEVICE_PORT, temperatureDevicePort);

    for(int i=0; i<TemperatureDeviceProperties.VALUES_COUNT; i++) {
      for(int j=0; j<3; j++) {
        cfg.writeFloat(KEY_TEMPERATURE + i + KEY_TEMPERATURE_FACTOR + j, temperatureFactors[i][j]);
      }
    }
        
    for(int i=0; i<chartIndexes.length; i++) {
      cfg.writeInteger(KEY_CHART_INDEX + Integer.toString(i), chartIndexes[i]);
    }

    cfg.save(CFG_FILE_NAME);
  }
} 
