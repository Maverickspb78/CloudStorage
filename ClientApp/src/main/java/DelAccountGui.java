import javax.swing.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

        yesButton.addActionListener(a->{
            try {
                if (delAccount().equals("account delete")){
                    dispose();
                    System.exit(0);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        noButton.addActionListener(a->{
            dispose();
        });
        
    }

    public String delAccount() throws IOException {
        String msg = "delAccount\n";
        cloudHandler.getOut().write(msg.getBytes(StandardCharsets.UTF_8));
        System.out.println("waiting in");
        String ans = (cloudHandler.readMsg(cloudHandler.getIn()));
        System.out.println(ans);

        return ans;
    }
}

