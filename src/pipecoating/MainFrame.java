package pipecoating;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.ListIterator;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

public final class MainFrame extends JFrame {
  private final static int TEMPERATURE_PANELS_COUNT = 4;
  private final static int TEMPERATURE_CHART_INDEX1 = 0;
  private final static int TEMPERATURE_CHART_INDEX2 = 1;
  private final static int TEMPERATURE_CHART_INDEX3 = 2;
  private final static int TEMPERATURE_CHART_INDEX4 = 3;
  private final static int[] TEMPERATURE_CHART_INDEXES = {
    TEMPERATURE_CHART_INDEX1,
    TEMPERATURE_CHART_INDEX2,
    TEMPERATURE_CHART_INDEX3,
    TEMPERATURE_CHART_INDEX4
  };
  private final static int THICKNESS_CHART_INDEX = 5;
  private final static int THICKNESS_OBSERVE_WINDOW_SIZE = 50;
  
  private final JLabel mThicknessA1ValueLabel = new JLabel();
  private final JLabel mThicknessB1ValueLabel = new JLabel();
  private final JLabel mThicknessB2ValueLabel = new JLabel();
  private final JLabel mThicknessB3ValueLabel = new JLabel();
  private final JLabel mThicknessB4ValueLabel = new JLabel();
  private final JLabel mTimeLabel = new JLabel();
  private final JComboBox mThicknessCaptionComboBox = new JComboBox();
  private final TimeChart mThicknessChart = new TimeChart();
  private final Timer mCalendarTimer;
  private final TemperatureThread mTemperatureThread = new TemperatureThread();
  private final TemperatureDeviceReader mTemperatureReader = new TemperatureDeviceReader();  
  private final ThicknessChartThread mThicknessThread = new ThicknessChartThread();
  private final Timer mThicknessLabelTimer;
  private final ThicknessDeviceReader mThicknessDeviceReader = new ThicknessDeviceReader();
  private final MockThicknessDevice mMockThicknessDevice = new MockThicknessDevice();
  private final MockTemperatureDevice mMockTemperatureDevice = new MockTemperatureDevice();  
  private final TemperaturePanelData[] mTemperaturePanelData = new TemperaturePanelData[TEMPERATURE_PANELS_COUNT];
  private final TimeChart.Serie[] mTemperatureSeries = new TimeChart.Serie[TemperatureDeviceProperties.VALUES_COUNT];
  private final TimeChart.Serie mThicknessA1Serie = new TimeChart.Serie();
  private final TimeChart.Serie mThicknessB1Serie = new TimeChart.Serie();
  private final TimeChart.Serie mThicknessB2Serie = new TimeChart.Serie();
  private final TimeChart.Serie mThicknessB3Serie = new TimeChart.Serie();
  private final TimeChart.Serie mThicknessB4Serie = new TimeChart.Serie();
  private final Float[] mTemperatureV = new Float[TemperatureDeviceProperties.VALUES_COUNT];
  private final LinkedList mThicknessA1ObserveWindow = new LinkedList();
  private final LinkedList mThicknessB1ObserveWindow = new LinkedList();
  private final LinkedList mThicknessB2ObserveWindow = new LinkedList();
  private final LinkedList mThicknessB3ObserveWindow = new LinkedList();
  private final LinkedList mThicknessB4ObserveWindow = new LinkedList();
  private Float mThicknessA1WindowValue;
  private Float mThicknessB1WindowValue;
  private Float mThicknessB2WindowValue;
  private Float mThicknessB3WindowValue;
  private Float mThicknessB4WindowValue;
  
  private static class TemperaturePanelData {
    JPanel rootPanel;
    TimeChart chart;
    JComboBox captionComboBox;
    JLabel captionLabel;
  }

  
  public MainFrame() {    
    for(int i=0; i<TemperatureDeviceProperties.VALUES_COUNT; i++) {
      mTemperatureSeries[i] = new TimeChart.Serie();
    }
    
    mThicknessA1Serie.color = new Color(255, 0, 0);
    mThicknessB1Serie.color = new Color(0, 128, 0);
    mThicknessB2Serie.color = new Color(0, 0, 128);
    mThicknessB3Serie.color = new Color(0, 128, 128);
    mThicknessB4Serie.color = new Color(128, 128, 0);
    
    createMainFrame();
    createMainPanel();
    
    for(int i = 0; i<mTemperaturePanelData.length; i++) {
      mTemperaturePanelData[i].captionComboBox.setSelectedIndex(
        Global.chartIndexes[TEMPERATURE_CHART_INDEXES[i]]);       
    }    
    int chartIndex = Global.chartIndexes[THICKNESS_CHART_INDEX];
    mThicknessCaptionComboBox.setSelectedIndex(chartIndex);
    updateThicknessSeriesVisibility();

    for(int i = 0; i < TEMPERATURE_PANELS_COUNT; i++) {
      final TimeChart chart = mTemperaturePanelData[i].chart;   
      chartIndex = Global.chartIndexes[TEMPERATURE_CHART_INDEXES[i]];
      chart.series.add(mTemperatureSeries[chartIndex]);
      setChartYAxis(chart, chartIndex);
    }
    
    mCalendarTimer = new Timer(500, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateCalendar();
      }
    });
    
    mThicknessLabelTimer = new Timer(200, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateThicknessLabels();  
      }
    });    
  }


  private void createMainFrame() {
    setTitle("Линия покрытия");

    setSize(1024, 768);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        mainFrame_windowClosing(e);
      }
      public void windowOpened(WindowEvent e) {
        mainFrame_windowOpened(e);
      }
    });

    setContentPane(new JPanel());
  }


  private void mainFrame_windowClosing(WindowEvent e) {  
    mMockTemperatureDevice.terminate();
    mTemperatureReader.terminate();
    mTemperatureThread.terminate();
    mMockThicknessDevice.terminate();
    mThicknessDeviceReader.terminate();
    mThicknessThread.terminate();
    mCalendarTimer.stop();
    mThicknessLabelTimer.stop();
    
    mTemperatureReader.join();
    mTemperatureThread.join();
    mMockThicknessDevice.join();
    mThicknessDeviceReader.join();
    mThicknessThread.join();
    
    System.exit(0);
  }


  private void mainFrame_windowOpened(WindowEvent e) {
    mMockTemperatureDevice.start();
    mTemperatureReader.start();    
    mTemperatureThread.start();
    mThicknessThread.start();
    mThicknessDeviceReader.start();
    mMockThicknessDevice.start();
    mCalendarTimer.start();
    mThicknessLabelTimer.start();
  }


  private void createMainPanel() {
    final JPanel mainPanel = (JPanel)getContentPane();
    mainPanel.setLayout(new BorderLayout());

    final JMenuBar menuBar = new JMenuBar();
    createMainMenu(menuBar);
    final JPanel workPanel = new JPanel();
    createWorkPanel(workPanel);

    mainPanel.add(BorderLayout.NORTH, menuBar);
    mainPanel.add(BorderLayout.CENTER, workPanel);
  }


  private void createMainMenu(JMenuBar menuBar) {
    final JMenu optionsMenu = new JMenu();
    final JMenu helpMenu = new JMenu();
    
    menuBar.add(optionsMenu);
    menuBar.add(helpMenu);

    optionsMenu.setText("Настройка");
    optionsMenu.setMnemonic('Н');
    
    final JMenuItem connectionMenuItem = new JMenuItem("Связь...");
    connectionMenuItem.setMnemonic('С');
    connectionMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        new ConnectionDialog(MainFrame.this).show();
      }
    });
    
    final JMenuItem factorsMenuItem = new JMenuItem("Коэффициенты...");
    factorsMenuItem.setMnemonic('К');
    factorsMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        new FactorsDialog(MainFrame.this).show();
      }
    });

    optionsMenu.add(connectionMenuItem);
    optionsMenu.add(factorsMenuItem);

    helpMenu.setText("Справка");
    helpMenu.setMnemonic('С');
    
    final JMenuItem aboutMenuItem = new JMenuItem("О программе...");
    aboutMenuItem.setMnemonic('О');
    aboutMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        new AboutDialog(MainFrame.this).show();
      }
    });

    helpMenu.add(aboutMenuItem);
  }


  private void createWorkPanel(JPanel rootPanel) {
    rootPanel.setLayout(new GridLayout(3, 2));
    for(int i = 0; i < TEMPERATURE_PANELS_COUNT; i++) {
      mTemperaturePanelData[i] = createTemperaturePanel(i);
      rootPanel.add(mTemperaturePanelData[i].rootPanel);
    }
    
    final JPanel workPanel5 = new JPanel();  
    createWorkPanel5(workPanel5);
    rootPanel.add(workPanel5);
    
    final JPanel workPanel6 = new JPanel();
    createWorkPanel6(workPanel6);
    rootPanel.add(workPanel6);
  }


  private TemperaturePanelData createTemperaturePanel(int index) {
    final int panelIndex = index;   

    final JComboBox captionComboBox = new JComboBox();
    for(int i=0; i<TemperatureDeviceProperties.TEMPERATURE_CAPTIONS.length; i++) {
      captionComboBox.addItem(TemperatureDeviceProperties.TEMPERATURE_CAPTIONS[i]);
    }
    captionComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        temperatureCaptionComboBox_actionPerformed(e, panelIndex);
      }
    });
    
    final JLabel captionLabel = new JLabel();
    createCaptionLabel(captionLabel);
    
    final JPanel workPanel11 = new JPanel();
    workPanel11.setLayout(new BorderLayout());
    workPanel11.add(BorderLayout.CENTER, captionComboBox);
    workPanel11.add(BorderLayout.EAST, captionLabel);
    
    final TimeChart chart = new TimeChart();
    createChart(chart);

    final JPanel rootPanel = new JPanel();    
    rootPanel.setLayout(new BorderLayout());
    rootPanel.add(BorderLayout.NORTH, workPanel11);
    rootPanel.add(BorderLayout.CENTER, chart);
    
    final TemperaturePanelData temperaturePanelData = new TemperaturePanelData();
    temperaturePanelData.rootPanel = rootPanel;
    temperaturePanelData.chart = chart;
    temperaturePanelData.captionLabel = captionLabel;
    temperaturePanelData.captionComboBox = captionComboBox;
    return temperaturePanelData;
  }


  private void temperatureCaptionComboBox_actionPerformed(ActionEvent e, int chartIndex) {      
    final TimeChart chart = mTemperaturePanelData[chartIndex].chart;
    
    final int index = mTemperaturePanelData[chartIndex].captionComboBox.getSelectedIndex();
    
    Global.chartIndexes[TEMPERATURE_CHART_INDEXES[chartIndex]] = index;
    Global.save();

    chart.series.clear();
    chart.series.add(mTemperatureSeries[index]);

    setChartYAxis(chart, index);
  }


  private void createCaptionLabel(JLabel label) {
    final Dimension size =
      new Dimension(80, (int)label.getPreferredSize().getHeight());
    label.setPreferredSize(size);
    label.setHorizontalAlignment(JLabel.RIGHT);
    label.setOpaque(true);
    label.setBackground(Color.white);
    label.setForeground(Color.black);
    final BzBorder border = new BzBorder();
    border.setInnerBorderWidth(1);
    border.setOuterBevelType(BzBorder.BEVEL_LOWERED);
    label.setBorder(border);
  }


  private void createChart(TimeChart chart) {
    final BzBorder border = new BzBorder();
    border.setOuterBevelType(BzBorder.BEVEL_LOWERED);
    chart.setBorder(border);
    chart.setCanvasColor(new Color(255, 255, 164));
    chart.setGridColor(new Color(230, 230, 0));
    chart.setAxisMarksColor(Color.black);
    setTemperatureChartXAxis(chart);
  }


  private void createWorkPanel5(JPanel workPanel5) {
    final JLabel caption5Label = new JLabel();
    caption5Label.setBorder(new EmptyBorder(3, 2, 2, 2));
    caption5Label.setText("Толщина покрытия");
    caption5Label.setForeground(Color.black);
    
    final JPanel workPanel2 = new JPanel();
    workPanel2.setLayout(new BoxLayout(workPanel2, BoxLayout.Y_AXIS));
    
    final String[] captions = {
      "На шве:",
      "На теле (канал 1):",
      "На теле (канал 2):",
      "На теле (канал 3):",
      "На теле (канал 4):"
    };
    final JLabel[] valueLabels = {
      mThicknessA1ValueLabel,  
      mThicknessB1ValueLabel,  
      mThicknessB2ValueLabel,  
      mThicknessB3ValueLabel,  
      mThicknessB4ValueLabel  
    };
    
    for(int i = 0; i < captions.length; i++) {
      final JPanel thicknessPanel = new JPanel();
      final BzBorder border = new BzBorder();
      border.setOuterBevelType(BzBorder.BEVEL_LOWERED);
      thicknessPanel.setBorder(border);
      thicknessPanel.setLayout(new BorderLayout());
      workPanel2.add(thicknessPanel);
      
      final JLabel captionLabel = new JLabel(captions[i]);
      createDeviceXiCaptionLabel(captionLabel);
      final JPanel captionPanel = new JPanel();
      captionPanel.setLayout(new BorderLayout());
      captionPanel.add(BorderLayout.CENTER, captionLabel);
      thicknessPanel.add(BorderLayout.CENTER, captionPanel);        
      
      final JPanel valuePanel = new JPanel();
      valuePanel.setLayout(new BzCenterLayout(BzCenterLayout.VERTICAL_LAYOUT));
      valuePanel.add(valueLabels[i]);
      createDeviceXiValueLabel(valueLabels[i]);
      thicknessPanel.add(BorderLayout.EAST, valuePanel);    
    }

    final JPanel workPanel26 = new JPanel();
    workPanel2.add(workPanel26);

    workPanel26.setLayout(new BoxLayout(workPanel26, BoxLayout.X_AXIS));
    workPanel26.add(Box.createGlue());
    workPanel26.add(mTimeLabel);

    mTimeLabel.setText("00:00:00");
    mTimeLabel.setHorizontalAlignment(JLabel.RIGHT);
    mTimeLabel.setVerticalAlignment(JLabel.CENTER);
    mTimeLabel.setBorder(new EmptyBorder(2, 8, 2, 8));
    mTimeLabel.setForeground(Color.black);
    mTimeLabel.setFont(new Font("Arial", Font.PLAIN, 18));
    
    
    final JPanel workPanel1 = new JPanel();
    final BzBorder border = new BzBorder();
    border.setOuterBevelType(BzBorder.BEVEL_LOWERED);
    border.setInnerBevelType(BzBorder.BEVEL_RAISED);
    workPanel1.setBorder(border);
    workPanel1.setLayout(new BorderLayout());
    workPanel1.add(BorderLayout.CENTER, caption5Label);
    
    workPanel5.setLayout(new BorderLayout());
    workPanel5.add(BorderLayout.NORTH, workPanel1);
    workPanel5.add(BorderLayout.CENTER, workPanel2);
  }


  private void createDeviceXiCaptionLabel(JLabel label) {
    label.setHorizontalAlignment(JLabel.LEFT);
    label.setBorder(new EmptyBorder(0, 8, 0, 8));
    label.setForeground(Color.black);
  }


  private void createDeviceXiValueLabel(JLabel label) {
    final BzBorder border = new BzBorder();
    border.setOuterBevelType(BzBorder.BEVEL_LOWERED);
    border.setInnerBorderWidth(1);
    label.setBorder(border);
    label.setHorizontalAlignment(JLabel.RIGHT);
    label.setForeground(Color.black);
    label.setBackground(Color.white);
    label.setOpaque(true);
    final Dimension size = new Dimension(80, 26);
    label.setPreferredSize(size);
  }


  private void createWorkPanel6(JPanel rootPanel) {
    final JPanel workPanel61 = new JPanel();
    workPanel61.setLayout(new BorderLayout());
    workPanel61.add(BorderLayout.CENTER, mThicknessCaptionComboBox);

    final String[] captions = {
      "Толщина покрытия (всё)",
      "Толщина покрытия на шве",
      "Толщина покрытия (канал 1)",
      "Толщина покрытия (канал 2)",
      "Толщина покрытия (канал 3)",
      "Толщина покрытия (канал 4)"        
    };
    for(int i = 0; i < captions.length; i++) {
      mThicknessCaptionComboBox.addItem(captions[i]);
    }
    mThicknessCaptionComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        caption6ComboBox_actionPerformed(e);
      }
    });

    createChart(mThicknessChart);
    mThicknessChart.setCanvasColor(new Color(200, 255, 200));
    mThicknessChart.setGridColor(new Color(80, 255, 80));
    mThicknessChart.setMinY(0);
    mThicknessChart.setMaxY(6);

    final TimeChart.AxisMarkList marks = mThicknessChart.yAxisMarks;
    
    for(int i = 1; i <= 5; i++) {
      marks.addItem(i, Integer.toString(i) + ".0");
    }

    final TimeChart.SerieList series = mThicknessChart.series;

    series.add(mThicknessA1Serie);
    series.add(mThicknessB1Serie);
    series.add(mThicknessB2Serie);
    series.add(mThicknessB3Serie);
    series.add(mThicknessB4Serie);

    rootPanel.setLayout(new BorderLayout());
    rootPanel.add(BorderLayout.NORTH, workPanel61);
    rootPanel.add(BorderLayout.CENTER, mThicknessChart);
  }


  private void caption6ComboBox_actionPerformed(ActionEvent e) {
    Global.chartIndexes[THICKNESS_CHART_INDEX] = mThicknessCaptionComboBox.getSelectedIndex();
    Global.save();
    updateThicknessSeriesVisibility();
  }
  
  
  private void updateThicknessSeriesVisibility() {
    final int index = Global.chartIndexes[THICKNESS_CHART_INDEX];

    mThicknessA1Serie.visible = index == 0 || index == 1;
    mThicknessB1Serie.visible = index == 0 || index == 2;
    mThicknessB2Serie.visible = index == 0 || index == 3;
    mThicknessB3Serie.visible = index == 0 || index == 4;
    mThicknessB4Serie.visible = index == 0 || index == 5;
  }



  private void setTemperatureChartXAxis(TimeChart chart) {
    chart.getXAxisMarkList().clear();

    final GregorianCalendar calendar = new GregorianCalendar();

    chart.setMaxX(calendar.getTime().getTime());
    chart.setMinX(calendar.getTime().getTime() - 30 /* minutes*/ * 60 * 1000);

    int hour;
    int min = calendar.get(Calendar.MINUTE);

    calendar.set(Calendar.MINUTE, (min/5)*5);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    for(int i=0; i<=6; i++) {
      hour = calendar.get(Calendar.HOUR_OF_DAY);
      min = calendar.get(Calendar.MINUTE);

      final String text = hour + ":"  + intToString02(min);

      chart.getXAxisMarkList().addItem(calendar.getTime().getTime(), text);

      Date date = calendar.getTime();
      date.setTime(date.getTime() - 5*60*1000);
      calendar.setTime(date);
    }
  }


  private void setThicknessChartXAxis(TimeChart chart) {
    chart.getXAxisMarkList().clear();

    final GregorianCalendar calendar = new GregorianCalendar();

    chart.setMaxX(calendar.getTime().getTime());
    chart.setMinX(calendar.getTime().getTime() - 5 /* seconds */ * 1000);

    calendar.set(Calendar.MILLISECOND, 0);

    for(int i=0; i<=6; i++) {
      final String text = calendar.get(Calendar.HOUR_OF_DAY) + ":" + 
                          intToString02(calendar.get(Calendar.MINUTE)) + ":" +
                          intToString02(calendar.get(Calendar.SECOND));

      chart.getXAxisMarkList().addItem(calendar.getTime().getTime(), text);

      final Date date = calendar.getTime();
      date.setTime(date.getTime() - 1*1000);
      calendar.setTime(date);
    }
  }


  private String temperatureToStr(Float value) {
    if(value == null) {
        return "";
    }

    NumberFormat nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(1);
    nf.setMinimumFractionDigits(1);
    return nf.format(value.doubleValue());
  }


  private String thicknessValueToStr(Float value) {
    if(value == null) {
      return "";
    }

    NumberFormat nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(2);
    nf.setMinimumFractionDigits(2);
    return nf.format(value.doubleValue());
  }
  
  
  private static String intToString02(int i) {      
    return i < 10 ? "0" + i : Integer.toString(i);  
  }


  private void cutSerieData(TimeChart.Serie serie, GregorianCalendar calendar) {
    double x = calendar.getTime().getTime();

    final TimeChart.DotList dots = serie.dots;

    int count = dots.size();

    for(int i=0; i<count; i++) {
      if(dots.getDot(0).x >= x)
        break;

      dots.delete(0);
    }
  }


  private void setChartYAxis(TimeChart chart, int index) {
    double minY = 0;
    double maxY = 0;
    int[] marks = null;
    
    switch(index) {
        case 0:
        case 5:
        case 6:
          maxY = 100;
          marks = new int[]{20, 40, 60, 80};
          break;
          
        case 1:
        case 2:    
        case 3:
          maxY = 250;
          marks = new int[]{50, 100, 150, 200};
          break;
          
        case 4:
        case 7:
        case 8:
          maxY = 200;
          marks = new int[]{50, 100, 150};
    }  
    
    if (marks != null) {
      chart.setMinY(minY);
      chart.setMaxY(maxY);

      final TimeChart.AxisMarkList list = chart.yAxisMarks;
 
      list.clear();
      
      for(int i = 0; i < marks.length; i++) {
        list.addItem(marks[i], Integer.toString(marks[i]));
      }
    }
  }


  private void addDot(TimeChart.Serie serie, GregorianCalendar calendar, Float y) {
    final double yValue = y == null ? 0 : y.doubleValue();
    final TimeChart.Dot dot = 
            new TimeChart.Dot(calendar.getTime().getTime(), yValue);

    if(y == null) {
      dot.flags = dot.EMPTY;
    }

    serie.dots.add(dot);
  }


  private void updateCalendar() {
    final GregorianCalendar calendar = new GregorianCalendar();

    final String txt = calendar.get(Calendar.HOUR_OF_DAY) + ":" + 
                      intToString02(calendar.get(Calendar.MINUTE)) + ":" +
                      intToString02(calendar.get(Calendar.SECOND));

    mTimeLabel.setText(txt);
  }


  private class TemperatureThread implements Runnable {
    final Thread mThread = new Thread(this);
    volatile boolean mTerminate;    
    final Float[] mValues = new Float[4];

    
    void start() {
      mThread.start();
    }
        
    
    void terminate() {
      mTerminate = true;  
    }
    
    
    void join() {
      try {
        mThread.join();
      }  
      catch(Exception ex) {          
      }
    }


    public void run() {
      refreshing: while(!mTerminate) {
        try {
          mThread.sleep(200);
        }
        catch(Exception ex) {
          break refreshing;    
        }
        refreshSeries();
        for(int i = 0; i < TEMPERATURE_PANELS_COUNT; i++) {
          if(mTerminate) {
            break refreshing;  
          }
          try {
            mThread.sleep(200);
          }
          catch(Exception ex) {
            break refreshing;  
          }
          final int chartIndex = i;
          EventQueue.invokeLater(new Runnable() {
            public void run() {
              setChart(chartIndex);
            }
          });
        }
      }
    }


    void setChart(int chartIndex) {
      final JLabel captionLabel = mTemperaturePanelData[chartIndex].captionLabel;  
      captionLabel.setText(temperatureToStr(mValues[chartIndex]) + " ");
      final TimeChart chart = mTemperaturePanelData[chartIndex].chart;
      setTemperatureChartXAxis(chart);
      chart.repaint();
    }


    void refreshSeries() {
      EventQueue.invokeLater(new Runnable() {
        public void run() {
          setSeries();
        }
      });
    }


    void setSeries() {
      final TemperatureDeviceReader.DeviceData deviceData = mTemperatureReader.getDeviceData();
      for(int i=0; i<TemperatureDeviceProperties.VALUES_COUNT; i++) {
        final Float value = deviceData.getValue(i);
        if(value == null) {
          mTemperatureV[i] = null;  
        }
        else {
          final float v = value.floatValue();
          final float[] a = Global.temperatureFactors[i];
          final float t = a[0] + a[1]*v + a[2]*v*v;
          mTemperatureV[i] = Float.valueOf(t);
        }
      }
      
              
      for(int i=0; i<TEMPERATURE_PANELS_COUNT; i++) {
        final int index = mTemperaturePanelData[i].captionComboBox.getSelectedIndex();
        mValues[i] = mTemperatureV[index];
      }

      for(int i=0; i<TemperatureDeviceProperties.VALUES_COUNT; i++) {
        final GregorianCalendar calendar = new GregorianCalendar();
        final TimeChart.Serie serie = mTemperatureSeries[i];

        addDot(serie, calendar, mTemperatureV[i]);

        calendar.add(GregorianCalendar.MINUTE, -31);
        cutSerieData(serie, calendar);
      }
    }
  }


  private class ThicknessChartThread implements Runnable {
    final Thread mThread = new Thread(this);
    volatile boolean mTerminate;

    
    void start() {
      mThread.start();
    }
    
    
    void terminate() {
      mTerminate = true;
    }
    
    
    void join() {
      try {  
        mThread.join();
      }
      catch(Exception ex) {          
      }
    }


    public void run() {
      while(!mTerminate) {
        try {
          mThread.sleep(25);
        }
        catch(Exception ex) {
        }
        
        EventQueue.invokeLater(new Runnable() {
          public void run() {
            if(!mTerminate) {
              setSeries();
              setThicknessChartXAxis(mThicknessChart);
              mThicknessChart.repaint();
            }
          }
        });
      }
    }


    void setSeries() {
      final GregorianCalendar calendar = new GregorianCalendar();
      
      final ThicknessDeviceReader.DeviceData deviceData = mThicknessDeviceReader.getDeviceData();
      
      pushToObserveWindow(mThicknessA1ObserveWindow, deviceData.getA1Value());
      pushToObserveWindow(mThicknessB1ObserveWindow, deviceData.getB1Value());
      pushToObserveWindow(mThicknessB2ObserveWindow, deviceData.getB2Value());
      pushToObserveWindow(mThicknessB3ObserveWindow, deviceData.getB3Value());
      pushToObserveWindow(mThicknessB4ObserveWindow, deviceData.getB4Value());
      
      mThicknessA1WindowValue = findMin(mThicknessA1ObserveWindow);
      mThicknessB1WindowValue = findMin(mThicknessB1ObserveWindow);
      mThicknessB2WindowValue = findMin(mThicknessB2ObserveWindow);
      mThicknessB3WindowValue = findMin(mThicknessB3ObserveWindow);
      mThicknessB4WindowValue = findMin(mThicknessB4ObserveWindow);

      addDot(mThicknessA1Serie, calendar, deviceData.getA1Value());
      addDot(mThicknessB1Serie, calendar, deviceData.getB1Value());
      addDot(mThicknessB2Serie, calendar, deviceData.getB2Value());
      addDot(mThicknessB3Serie, calendar, deviceData.getB3Value());
      addDot(mThicknessB4Serie, calendar, deviceData.getB4Value());

      calendar.add(GregorianCalendar.SECOND, -5);
      cutSerieData(mThicknessA1Serie, calendar);
      cutSerieData(mThicknessB1Serie, calendar);
      cutSerieData(mThicknessB2Serie, calendar);
      cutSerieData(mThicknessB3Serie, calendar);
      cutSerieData(mThicknessB4Serie, calendar);
    }
    
    
    void pushToObserveWindow(LinkedList window, Float newValue) {
      window.addLast(newValue);
      if (window.size() > THICKNESS_OBSERVE_WINDOW_SIZE) {
        window.removeFirst();
      }
    }
    
    
    Float findMin(LinkedList window) {
      final ListIterator iterator = window.listIterator();
      Float min = null;
      while(iterator.hasNext()) {
        final Float currValue = (Float)iterator.next();
        if(currValue != null) {
          if(min == null || min.compareTo(currValue) > 0) {
            min = currValue;    
          }
        }
      }
      return min;
    }
  }


  private void updateThicknessLabels() {
    final JLabel[] labels = {
      mThicknessA1ValueLabel,
      mThicknessB1ValueLabel,  
      mThicknessB2ValueLabel,
      mThicknessB3ValueLabel,
      mThicknessB4ValueLabel      
    };
    final Float[] values = {
      mThicknessA1WindowValue,
      mThicknessB1WindowValue,
      mThicknessB2WindowValue,
      mThicknessB3WindowValue,
      mThicknessB4WindowValue
    };
    for(int i = 0; i < labels.length; i++) {
      labels[i].setText(thicknessValueToStr(values[i]) + " ");  
    }
  } 
}
