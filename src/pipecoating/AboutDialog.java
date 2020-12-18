package pipecoating;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;


public final class AboutDialog extends JDialog {
  public AboutDialog(Frame parent) {
    super(parent, "О программе");

    setResizable(false);
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
    final JLabel programLabel = new JLabel("\"Линия покрытия труб\"");
    programLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    programLabel.setFont(new Font("Arial", Font.BOLD, 20));

    final JLabel versionLabel = new JLabel("Версия: " + Global.VERSION);
    versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    versionLabel.setFont(new Font("Arial", Font.BOLD, 12));

    final JLabel dateLabel = new JLabel(Global.RELEASE_DATE);
    dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    dateLabel.setFont(new Font("Arial", Font.BOLD, 10));
    
    final JPanel infoPanel0 = new JPanel();
    infoPanel0.setBorder(new EmptyBorder(8, 8, 8, 8));
    infoPanel0.setLayout(new BoxLayout(infoPanel0, BoxLayout.Y_AXIS));
    infoPanel0.add(programLabel);
    infoPanel0.add(Box.createRigidArea(new Dimension(0, 3)));
    infoPanel0.add(versionLabel);
    infoPanel0.add(Box.createRigidArea(new Dimension(0, 3)));
    infoPanel0.add(dateLabel);


    final JPanel buttonsPanel = new JPanel();
    final BzBorder border = new BzBorder();
    border.setOuterBevelType(BzBorder.BEVEL_LOWERED);
    buttonsPanel.setBorder(border);

    final JButton closeButton = new JButton("Закрыть");
    closeButton.setDefaultCapable(true);
    getRootPane().setDefaultButton(closeButton);
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hide();
      }
    });
    
    final BzCenterLayout layout = new BzCenterLayout();
    layout.setInsets(8, 8, 8, 8);
    buttonsPanel.setLayout(layout);
    buttonsPanel.add(closeButton);
    
    mainPanel.setLayout(new BorderLayout());
    mainPanel.add(BorderLayout.CENTER, infoPanel0);
    mainPanel.add(BorderLayout.SOUTH, buttonsPanel);
  }
} 
