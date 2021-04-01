import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class AuthorizationGUI extends JDialog {
    private FileCloudHandler cloudHandler;
    private JPanel contentPaneA;
    private JTextField loginFieldA;
    private JPasswordField passFieldA;
    private JLabel loginField;
    private JLabel passField;
    private JButton authButtonA;
    private JButton regButtonA;
    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;
    private Path serverPath;


    public AuthorizationGUI() throws IOException {

        setContentPane(contentPaneA);
        setVisible(true);
        setModal(true);
        socket = new Socket("localhost", 1234);

//        socket = cloudHandler.getSocket();
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
//        cloudHandler.setIn(in);
//        cloudHandler.setOut(out);
        cloudHandler = new FileCloudHandler(serverPath, Path.of(""), in, out, socket );
//        this.socket = socket;
//        this.in = in;
//        this.out = out;
//        this.cloudHandler = cloudHandler;


        authButtonA.addActionListener(a -> {
            try {
                if (authorizatior(loginFieldA, passFieldA) == 1) {
                    System.out.println("if buttonAuth");
                    setVisible(false);
                    CloudGUI cloudGUI = new CloudGUI(socket, cloudHandler);
                    cloudGUI.setSize(800, 800);
                    cloudGUI.setLocationRelativeTo(null);
                    cloudGUI.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                    cloudGUI.setVisible(true);
                } else {
                    loginFieldA.setText("Wrong login or password");
                    passFieldA.setText("");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        regButtonA.addActionListener(a -> {
            try {
                if (registr(loginFieldA, passFieldA) == 1) {
                    System.out.println("if buttonReg");
                    setVisible(false);
                    CloudGUI cloudGUI = new CloudGUI(socket, cloudHandler);
                    cloudGUI.setSize(800, 800);
                    cloudGUI.setLocationRelativeTo(null);
                    cloudGUI.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                    cloudGUI.setVisible(true);
                } else {
                    loginFieldA.setText("this login already used");
                    passFieldA.setText("");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    public int authorizatior(JTextField taAuth, JPasswordField passwordField) throws IOException {
        String msg = "auth\n" + taAuth.getText() + "\n" + passwordField.getText() + "\n";
        out.write(msg.getBytes(StandardCharsets.UTF_8));
        System.out.println("waiting in");
        int b = Integer.parseInt(cloudHandler.readMsg(in));
        System.out.println(b);
        return b;
    }

    public int registr(JTextField taAuth, JPasswordField passwordField) throws IOException {
        String msg = "reg\n" + taAuth.getText() + "\n" + passwordField.getText() + "\n";
        out.write(msg.getBytes(StandardCharsets.UTF_8));
        System.out.println("waiting in");
        int b = Integer.parseInt(cloudHandler.readMsg(in));
        System.out.println(b);
        return b;
    }


//    public static void main(String[] args) throws IOException {
////        AuthorizationGUI dialogA = new AuthorizationGUI();
////        dialogA.pack();
////        dialogA.setVisible(true);
////        System.exit(0);
//    }

}
