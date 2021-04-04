import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ChangePassGUI extends JDialog {
    private JPasswordField passwordField1;
    private JButton button1;
    private JButton button2;
    private JPanel panel;

    private FileCloudHandler cloudHandler;


    public ChangePassGUI(FileCloudHandler cloudHandler) {
        this.cloudHandler = cloudHandler;
        setContentPane(panel);
        setSize(220, 120);
        setLocationRelativeTo(null);
        setVisible(true);
        button1.addActionListener(a -> {
            try {
                if (changePass().equals("password changed")){
                    dispose();

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        button2.addActionListener(a -> {
        dispose();
        });
    }

    public String changePass() throws IOException {
        String msg = "changePass\n" + passwordField1.getText() + "\n";
        cloudHandler.getOut().write(msg.getBytes(StandardCharsets.UTF_8));
        System.out.println("waiting in");
        String ans = (cloudHandler.readMsg(cloudHandler.getIn()));
        System.out.println(ans);

        return ans;
    }
}



