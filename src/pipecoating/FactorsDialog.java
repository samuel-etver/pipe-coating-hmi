package pipecoating;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;


public final class FactorsDialog extends JDialog {
  public FactorsDialog(Frame parent) {
    super(parent, "Коэффициенты преобразования");

    setModal(true);

    final JPanel mainPanel = new JPanel();
    setContentPane(mainPanel);
    createMainPanel(mainPanel);

    pack();

    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation((int)(screenSize.getWidth() - getWidth())/2,
                (int)(screenSize.getHeight() - getHeight())/2);
  }


  private void createMainPanel(JPanel mainPanel) {
    final FactorsTableModel factorsTableModel = new FactorsTableModel();
    final JTable factorsTable = new JTable(factorsTableModel);
    factorsTable.setPreferredScrollableViewportSize(new Dimension(520, 140));
    factorsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    final TableColumn column = factorsTable.getColumnModel().getColumn(0);
    column.setPreferredWidth(320);
    final ExponentRenderer renderer = new ExponentRenderer();
    factorsTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
    factorsTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
    factorsTable.getColumnModel().getColumn(3).setCellRenderer(renderer);

    final JScrollPane factorsScrollPane = new JScrollPane();
    factorsScrollPane.setViewportView(factorsTable);

    final JPanel innerFactorsPanel = new JPanel();
    innerFactorsPanel.setLayout(new BorderLayout());
    innerFactorsPanel.add(factorsScrollPane);

    final JPanel factorsPanel = new JPanel();
    factorsPanel.setLayout(new BorderLayout());
    factorsPanel.add(BorderLayout.CENTER, innerFactorsPanel);
    factorsPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

    final JPanel innerButtonsPanel = new JPanel();
    final BzCenterLayout layout = new BzCenterLayout(BzCenterLayout.HORIZONTAL_LAYOUT);
    layout.setInsets(8, 8, 8, 8);
    layout.setGap(8);
    innerButtonsPanel.setLayout(layout);

    final JPanel buttonsPanel = new JPanel();
    buttonsPanel.setLayout(new BorderLayout());
    buttonsPanel.add(BorderLayout.EAST, innerButtonsPanel);
    final BzBorder border = new BzBorder();
    border.setOuterBevelType(BzBorder.BEVEL_LOWERED);
    buttonsPanel.setBorder(border);

    final JButton okButton = new JButton("Ввод");
    okButton.setDefaultCapable(true);
    getRootPane().setDefaultButton(okButton);
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        factorsTableModel.apply();
        Global.save();
        hide();
      }
    });
    
    final JButton cancelButton = new JButton("Отмена");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hide();
      }
    });
    
    innerButtonsPanel.add(okButton);
    innerButtonsPanel.add(cancelButton);
      
    mainPanel.setLayout(new BorderLayout());
    mainPanel.add(BorderLayout.CENTER, factorsPanel);
    mainPanel.add(BorderLayout.SOUTH, buttonsPanel);
  }


  private class FactorsTableModel extends AbstractTableModel {
    final String[] columnNames = {"",  "A0", "A1", "A2"};
    final String[] rowNames = TemperatureDeviceProperties.TEMPERATURE_CAPTIONS;
    Float[] a0;
    Float[] a1;
    Float[] a2;


    public FactorsTableModel() {
      final int n = TemperatureDeviceProperties.VALUES_COUNT;

      a0 = new Float[n];
      a1 = new Float[n];
      a2 = new Float[n];

      for(int i = 0; i < n; i++) {
        a0[i] = new Float(Global.temperatureFactors[i][0]);
        a1[i] = new Float(Global.temperatureFactors[i][1]);
        a2[i] = new Float(Global.temperatureFactors[i][2]);
      }
    }


    public void apply() {
      for(int i = 0; i < TemperatureDeviceProperties.VALUES_COUNT; i++) {
        Global.temperatureFactors[i][0] = a0[i].floatValue();
        Global.temperatureFactors[i][1] = a1[i].floatValue();
        Global.temperatureFactors[i][2] = a2[i].floatValue();
      }
    }


    public int getColumnCount() {
      return 4;
    }


    public int getRowCount() {
      return rowNames.length;
    }


    public String getColumnName(int col) {
      return columnNames[col];
    }


    public Object getValueAt(int row, int col) {
      switch(col) {
        case 0:  return rowNames[row];
        case 1:  return a0[row];
        case 2:  return a1[row];
        case 3:  return a2[row];
        default: return "";
      }
    }


    public boolean isCellEditable(int row, int col) {
      return col > 0;
    }


    public void setValueAt(Object value, int row, int col) {
      switch(col) {
        case 1: a0[row] = (Float)value; break;
        case 2: a1[row] = (Float)value; break;
        case 3: a2[row] = (Float)value;
      }
    }


    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }
  }


  private final class ExponentRenderer extends JLabel implements TableCellRenderer {
    Border focusBorder = null;
    Border emptyBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);


    public ExponentRenderer() {
      super();
      setHorizontalAlignment(JLabel.RIGHT);
      setOpaque(true);
    }


    public Component getTableCellRendererComponent(JTable table, Object object,
        boolean isSelected, boolean hasFocus, int row, int column) {
      setText(Float.toString(((Number)object).floatValue()));
      setForeground(table.getForeground());
      setBackground(table.getBackground());
      setBorder(emptyBorder);
      if(hasFocus) {
        if(focusBorder == null) {
          focusBorder =
            BorderFactory.createLineBorder(table.getSelectionBackground().darker());
        }
        setBorder(focusBorder);
      }
      else {
        if(isSelected) {
          setBackground(table.getSelectionBackground());
          setForeground(table.getSelectionForeground());
        }
      }
        
      setFont(table.getFont());

      return this;
    }
  }
} 
