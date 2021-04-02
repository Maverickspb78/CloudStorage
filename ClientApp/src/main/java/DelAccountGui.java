import javax.swing.*;

public class DelAccountGui extends JDialog {
    private final FileCloudHandler cloudHandler;
    private JButton yesButton;
    private JButton noButton;
    private JPanel panel;

    public DelAccountGui(FileCloudHandler cloudHandler) {
        this.cloudHandler = cloudHandler;
        setContentPane(panel);
        setSize(220, 120);
        setLocationRelativeTo(null);
        setVisible(true);

        
    }
}

