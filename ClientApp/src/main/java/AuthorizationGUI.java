import javax.swing.*;

public class AuthorizationGUI extends JDialog {
    private JPanel contentPaneA;
    private JTextField loginFieldA;
    private JPasswordField passFieldA;
    private JLabel loginField;
    private JLabel passField;
    private JButton authButtonA;
    private JButton regButtonA;
    private JButton buttonOK;

    public AuthorizationGUI() {
        setContentPane(contentPaneA);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
    }

    public static void main(String[] args) {

    }
}
