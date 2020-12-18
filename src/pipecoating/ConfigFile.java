package pipecoating;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Vector;


public class ConfigFile {
  private List mList = new List();


  public void clear() {
    mList.clear();
  }


  public void writeString(String key, String value) {
    final int index = mList.find(key);

    if(index < 0) {
      mList.add(key, value);
    }
    else {
      mList.setValue(index, value);
    }
  }


  public void writeBoolean(String key, boolean value) {
    writeString(key, Boolean.toString(value));
  }


  public void writeInteger(String key, int value) {
    writeString(key, Integer.toString(value));
  }


  public void writeFloat(String key, float value) {
    writeString(key, Float.toString(value));
  }


  public String readString(String key, String defValue) {
    final int index = mList.find(key);

    return index < 0 
            ? defValue
            : mList.getValue(index);
  }


  public boolean readBoolean(String key, boolean defValue) {
    final String valueStr = readString(key, null);

    if("true".equalsIgnoreCase(valueStr)) {
      return true;
    }
    if("false".equalsIgnoreCase(valueStr)) {
      return false;
    }
    
    return defValue;
  }


  public int readInteger(String key, int defValue) {
    int result = defValue;

    try {
      result = Integer.parseInt(readString(key, ""));
    }
    catch(Exception ex) {
    }

    return result;
  }


  public float readFloat(String key, float defValue) {
    float result = defValue;

    try {
      result = Float.parseFloat(readString(key, ""));
    }
    catch(Exception ex) {
    }

    return result;
  }


  public boolean save(String fileName) {
    final StringBuffer buffer = new StringBuffer();

    final int n = mList.size();
    for(int i=0; i<n; i++) {
      buffer.append(mList.getKey(i));
      buffer.append("=");
      buffer.append(mList.getValue(i));
      buffer.append("\n");
    }    
      
    boolean result = false;
    FileOutputStream stream = null;

    try {
      stream = new FileOutputStream(fileName);
      stream.write(buffer.toString().getBytes(StandardCharsets.UTF_8));
      result = true;
    }
    catch(Exception exception) {
    }

    try {
      stream.close();
    }
    catch(Exception exception) {
    }

    return result;
  }


  public boolean load(String fileName) {
    BufferedReader reader = null;

    try {
      reader = new BufferedReader(
                new InputStreamReader(
                 new FileInputStream(fileName), StandardCharsets.UTF_8));   
    }
    catch(Exception ex) {
      return false;
    }


    boolean result = false;

    try {
      boolean done = false;

      final StringBuffer line = new StringBuffer();
      final StringBuffer temp = new StringBuffer();

      while(!done) {
        line.setLength(0);

        int b = reader.read();
        while(b >= 0) {
          if(b == 0x0A) break;
          if(b == 0x0D) break;

          line.append((char)b);

          b = reader.read();
        }

        int n = line.length();
        int i;

        temp.setLength(0);

        for(i=0; i<n; i++) {
          char ch = line.charAt(i);
          if(ch == '=')
            break;
          temp.append(ch);
        }

        final String key = temp.toString().trim();

        temp.setLength(0);

        for(i=i+1; i<n; i++)
          temp.append(line.charAt(i));

        final String value = temp.toString().trim();

        if(key.length() > 0 || value.length() > 0)
          mList.add(key, value);

        done = b < 0;
      }

      result = true;
    }
    catch(IOException exception) {
    }

    try {
      reader.close();
    }
    catch(IOException exception) {
    }

    return result;
  }


  private class KeyValue {
    public String key;
    public String value;


    public KeyValue(String key, String value) {
      this.key = key;
      this.value = value;
    }
  }


  private class List {
    private final Vector vector = new Vector();


    public void clear() {
      vector.clear();
    }


    public void add(String key, String value) {
      vector.add(new KeyValue(key, value));
    }


    public KeyValue getItem(int index) {
      return (KeyValue)vector.get(index);
    }


    public void setItem(int index, KeyValue item) {
      vector.set(index, item);
    }


    public String getKey(int index) {
      return getItem(index).key;
    }


    public String getValue(int index) {
      return getItem(index).value;
    }


    public void setValue(int index, String value) {
      getItem(index).value = value;
    }


    public int size() {
      return vector.size();
    }


    public int find(String key) {
      final int n = size();

      for(int i=0; i<n; i++) {
        if(getKey(i).equals(key)) {
          return i;
        }
      }

      return -1;
    }
  }
}
