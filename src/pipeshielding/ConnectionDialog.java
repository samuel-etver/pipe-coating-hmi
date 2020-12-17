package pipeshielding;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

public final class ConnectionDialog extends JDialog {
  private final JTextField thicknessDevicePortTextField = new JTextField();
  private final JTextField temperatureDevicePortTextField = new JTextField();
  

  public ConnectionDialog(Frame parent) {
    super(parent, "Связь");

    setResizable(false);
    setModal(true);

    final JPanel mainPanel = new JPanel();
    setContentPane(mainPanel);
    createMainPanel(mainPanel);

    addWindowListener(new WindowAdapter() {
      public void windowOpened(WindowEvent e) {
        thicknessDevicePortTextField.requestFocus();
      }
    });

    pack();

    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation((int)(screenSize.getWidth() - getWidth())/2,
                (int)(screenSize.getHeight() - getHeight())/2);
  }


  private void createMainPanel(JPanel mainPanel) {
    final JPanel tabsPanel = new JPanel();
    final JPanel buttonsPanel = new JPanel();
    
    mainPanel.setLayout(new BorderLayout());    
    mainPanel.add(BorderLayout.CENTER, tabsPanel);
    mainPanel.add(BorderLayout.EAST, buttonsPanel);

    createTabsPanel0(tabsPanel);
    createButtonsPanel(buttonsPanel);
  }


  private void createTabsPanel0(JPanel tabsPanel) {
    final JLabel thicknessDevicePortLabel = new JLabel("Порт:");    
    final Dimension size = new Dimension(80,
      (int)thicknessDevicePortTextField.getPreferredSize().getHeight());
    thicknessDevicePortTextField.setPreferredSize(size);
    thicknessDevicePortTextField.setText(Integer.toString(Global.thicknessDevicePort));
    
    final JPanel thicknessDevicePortPanel = new JPanel();
    final BzBorder border = new BzBorder();
    border.setOuterBevelType(BzBorder.BEVEL_LOWERED);
    border.setInnerBevelType(BzBorder.BEVEL_RAISED);
    thicknessDevicePortPanel.setBorder(border);
    final BzCenterLayout layout = new BzCenterLayout(BzCenterLayout.VERTICAL_LAYOUT);
    layout.setInsets(16, 80, 20, 80);
    thicknessDevicePortPanel.setLayout(layout);
    thicknessDevicePortPanel.add(thicknessDevicePortLabel);
    thicknessDevicePortPanel.add(thicknessDevicePortTextField);

    final JLabel temperatureDevicePortLabel = new JLabel("Порт:");
    temperatureDevicePortTextField.setPreferredSize(size);
    temperatureDevicePortTextField.setText(Integer.toString(Global.temperatureDevicePort));

    final JPanel temperatureDevicePortPanel = new JPanel();
    temperatureDevicePortPanel.setBorder(border);
    temperatureDevicePortPanel.setLayout(layout);
    temperatureDevicePortPanel.add(temperatureDevicePortLabel);
    temperatureDevicePortPanel.add(temperatureDevicePortTextField);

    final JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.add("Толщиномер", thicknessDevicePortPanel);
    tabbedPane.add("Температура", temperatureDevicePortPanel);
    
    tabsPanel.setLayout(new BorderLayout());
    tabsPanel.add(BorderLayout.NORTH, Box.createRigidArea(new Dimension(0, 8)));
    tabsPanel.add(BorderLayout.SOUTH, Box.createRigidArea(new Dimension(0, 8)));
    tabsPanel.add(BorderLayout.WEST, Box.createRigidArea(new Dimension(8, 0)));
    tabsPanel.add(BorderLayout.EAST, Box.createRigidArea(new Dimension(8, 0)));
    tabsPanel.add(BorderLayout.CENTER, tabbedPane);
  }


  private void createButtonsPanel(JPanel buttonsPanel) {
    final JPanel innerPanel = new JPanel();
    
    buttonsPanel.setLayout(new BorderLayout());
    buttonsPanel.add(BorderLayout.NORTH, innerPanel);

    final BzBorder border = new BzBorder();
    border.setOuterBevelType(BzBorder.BEVEL_LOWERED);
    buttonsPanel.setBorder(border);

    final BzCenterLayout layout = new BzCenterLayout(BzCenterLayout.VERTICAL_LAYOUT);
    layout.setInsets(8, 8, 8, 8);
    layout.setGap(8);
    innerPanel.setLayout(layout);

    final JButton okButton = new JButton("Ввод");
    okButton.setDefaultCapable(true);
    getRootPane().setDefaultButton(okButton);
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okButton_actionPerformed();
      }
    });
    
    final JButton cancelButton = new JButton("Отмена");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hide();
      }
    });

    innerPanel.add(okButton);
    innerPanel.add(cancelButton);
  }


  private void okButton_actionPerformed() {
    try {
      Global.thicknessDevicePort = 
       Integer.parseInt(thicknessDevicePortTextField.getText().trim());
    }
    catch(Exception exception) {
    }
    
    try {
      Global.temperatureDevicePort = 
       Integer.parseInt(temperatureDevicePortTextField.getText().trim());
    }
    catch(Exception ex) {        
    }

    Global.save();
    hide();
  }
} 
