

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class CloudGUI extends JDialog {
    private JPanel contentPane;
    private JButton removeButton;
    private JButton exitButton;
    private JList list1;
    private JList list2;
    private JTextField taC;
    private JTextField taS;
    private JButton downloadButton;
    private JButton uploadButton;
    private JButton createFolder;
    private JButton searchButton;
    private JComboBox menuBox;
    private JPanel panel1;
    private JLabel infoLable;
    private JLabel sizeLable;


    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private DefaultListModel<String> myModel = new DefaultListModel<>();
    private DefaultListModel<String> myModel2 = new DefaultListModel<>();
    private Path serverPath;
    private Path clientPath;
    private FileCloudHandler cloudHandler;
    private ListHandler listHandler;

    public FileCloudHandler getCloudHandler() {
        return cloudHandler;
    }

    public void setCloudHandler(FileCloudHandler cloudHandler) {
        this.cloudHandler = cloudHandler;
    }

    public ListHandler getListHandler() {
        return listHandler;
    }

    public void setListHandler(ListHandler listHandler) {
        this.listHandler = listHandler;
    }

    public Path getServerPath() {
        return serverPath;
    }

    public void setServerPath(Path serverPath) {
        this.serverPath = serverPath;
    }

    public CloudGUI(Socket socket, FileCloudHandler cloudHandler) throws IOException {
        this.socket = socket;
        this.cloudHandler = cloudHandler;

        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
//        cloudHandler = new FileCloudHandler(serverPath,clientPath, in , out , socket);
        listHandler = new ListHandler(cloudHandler.getServerPath(), cloudHandler.getClientPath(), socket, in, out);


//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(new Dimension(width,height));
//        frame.setLocationRelativeTo(null);

        setContentPane(contentPane);
        setVisible(true);
        setModal(true);
        list1.setModel(myModel);
        list2.setModel(myModel2);

        serverPath = cloudHandler.getServerPath();
        clientPath = cloudHandler.getClientPath();
//        listHandler.setServerPath(serverPath);
//        listHandler.setClientPath(clientPath);

        listHandler.fillList(myModel, serverPath);
        listHandler.clientList(myModel2, clientPath, "out");


        downloadButton.addActionListener(a -> {
            cloudHandler.setServerPath(serverPath);
            cloudHandler.setClientPath(clientPath);
            cloudHandler.downloadFile(taS.getText());
            try {
                listHandler.fillList(myModel, serverPath);
                listHandler.clientList(myModel2, clientPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        uploadButton.addActionListener(a -> {
            cloudHandler.setClientPath(clientPath);
            cloudHandler.sendFile(taC.getText());
            try {
                listHandler.fillList(myModel, serverPath);
                listHandler.clientList(myModel2, clientPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        removeButton.addActionListener(a -> {
            if (taC.getText().equals("")) {
                cloudHandler.setServerPath(serverPath);
                cloudHandler.remove(taS.getText(), "server");
                try {
                    listHandler.fillList(myModel, serverPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (taS.getText().equals("")) {
                cloudHandler.setClientPath(clientPath);
                cloudHandler.remove(taC.getText(), "client");
                listHandler.clientList(myModel2, clientPath);
            }
        });

        createFolder.addActionListener(a -> {
            cloudHandler.setClientPath(clientPath);
            cloudHandler.setServerPath(serverPath);
            if ((taC.getText().equals("")) && (taS.getText().equals(""))) {
                System.out.println("null name folder");
            } else if (taC.getText().equals("")) {
                try {
                    cloudHandler.createFolder(taS, "left");
                    listHandler.fillList(myModel, serverPath);
                    taS.setText("");
                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else if (taS.getText().equals("")) {
                System.out.println(clientPath);
                if (listHandler.getClientPath().toString().length() > 4) {
                    try {
                        cloudHandler.createFolder(taC, "right");
                        listHandler.fillList(myModel2, clientPath);
                        taC.setText("");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                listHandler.clientList(myModel2, clientPath);
            }
        });

        searchButton.addActionListener(a -> {

        });

        exitButton.addActionListener(a -> {

        });

        list1.addListSelectionListener(a -> {
            taC.setText("");
            taS.setText((String) list1.getSelectedValue());

        });
        list2.addListSelectionListener(a -> {
            taS.setText("");
            taC.setText((String) list2.getSelectedValue());
        });

        //addListSelectionListener to the server window
        list1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (taS.getText().equals("...")) {
                        String[] p = serverPath.toString().split("\\\\");
                        if (p.length > 1) {
                            String pah = "";
                            for (int i = 0; i < p.length - 1; i++) {
                                pah += p[i] + "\\";
                            }
                            serverPath = Paths.get(pah);
                        }
                    } else {
                        serverPath = Paths.get(serverPath + File.separator + taS.getText());
                    }
                    try {
                        listHandler.fillList(myModel, serverPath);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }

                }
            }
        });

        //addListSelectionListener to the client window

        list2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                if (e.getClickCount() == 2) {
                    if ((clientPath.toString().length() < 4) && (taC.getText().contains(":\\"))) {
                        clientPath = Path.of(taC.getText());
                        listHandler.clientList(myModel2, clientPath);
                    }
                    if (Files.isDirectory(Paths.get(clientPath.toString() + File.separator + taC.getText()))) {
                        if ((taC.getText().equals("..."))) { //|| taS.getText().equals("...")
                            if (clientPath.toAbsolutePath().normalize().getParent() != null) {
                                clientPath = clientPath.normalize().toAbsolutePath().getParent();
                                listHandler.clientList(myModel2, clientPath);
                            } else {
                                clientPath = Path.of(taC.getText());
                                listHandler.clientList(myModel2, clientPath, "out");
                            }
                        } else {
                            clientPath = Path.of(clientPath + File.separator + taC.getText());
                            listHandler.clientList(myModel2, clientPath);
                        }
                    }
                }

            }

        });
    }


//    public static void main(String[] args) throws IOException {
////        CloudGUI dialog = new CloudGUI();
////        dialog.pack();
////        dialog.setVisible(true);
////        System.exit(0);
//    }

}
