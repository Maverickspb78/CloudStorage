import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CloudGUI extends JDialog {
    private JFrame frame;
    private JPanel contentPane;
    private JButton removeButton;
    private JButton exitButton;
    private JList list1;
    private JList list2;
    private JTextField textField1;
    private JTextField textField2;
    private JButton downloadButton;
    private JButton uploadButton;
    private JButton createFolder;
    private JButton searchButton;
    private JComboBox menuBox;
    private JPanel panel1;
    private final int height = 800;
    private final int width = 900;

    public CloudGUI() {

//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(new Dimension(width,height));
//        frame.setLocationRelativeTo(null);

        setContentPane(contentPane);
        getContentPane().setSize(width, height);
        setVisible(true);
        setModal(true);

        getRootPane().setDefaultButton(removeButton);

        removeButton.addActionListener(a-> {

        });

        exitButton.addActionListener(a-> {

        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE

        downloadButton.addActionListener(a-> {

        });
        uploadButton.addActionListener(a-> {

        });

        removeButton.addActionListener(a->{

        });

        createFolder.addActionListener(a->{

        });

        searchButton.addActionListener(a->{

        });

        exitButton.addActionListener(a->{

        });
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        CloudGUI dialog = new CloudGUI();
        dialog.pack();
        dialog.setVisible(true);
        dialog.setSize(800,800);
        dialog.setLocationRelativeTo(null);
        System.exit(0);
    }

    private void createUIComponents() {

    }
}
