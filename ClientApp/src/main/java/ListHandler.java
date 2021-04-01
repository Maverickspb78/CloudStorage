import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListHandler {
    private Path serverPath; //= Paths.get("server");
    private Path clientPath; // = Paths.get("c:\\");
    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;

    public ListHandler(Path serverPath, Path clientPath, Socket socket, DataInputStream in, DataOutputStream out) throws IOException {
        System.out.println(socket);
        this.serverPath = serverPath;
        this.socket = socket;
        System.out.println(socket);
//        out = new DataOutputStream(socket.getOutputStream());
//        in = new DataInputStream(socket.getInputStream());
    }

    public Path getServerPath() {
        return serverPath;
    }

    public void setServerPath(Path serverPath) {
        this.serverPath = serverPath;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public DataInputStream getIn() {
        return in;
    }

    public void setIn(DataInputStream in) {
        this.in = in;
    }

    public DataOutputStream getOut() {
        return out;
    }

    public void setOut(DataOutputStream out) {
        this.out = out;
    }

    public Path getClientPath() {
        return clientPath;
    }

    public void setClientPath(Path clientPath) {
        this.clientPath = clientPath;
    }

    public void clientList(DefaultListModel<String> myModel2, Path clientPath) {
        this.clientPath = clientPath;
        if (clientPath.toString().equals("...")) {
            clientList(myModel2, clientPath, "out");

        } else {
            File file = new File(clientPath.toString());
            String[] files = file.list();
            myModel2.clear();
            myModel2.addElement("...");
            if (files != null) {
                for (String fil : files) {
                    myModel2.addElement(fil);
                }
            }
        }
    }

    public void clientList(DefaultListModel<String> myModel2, Path clientPath, String out) {
        this.clientPath = clientPath;
        String arg = FileSystems.getDefault().getRootDirectories().toString();
        arg = arg.replace("[", "");
        arg = arg.replace("]", "");
        arg = arg.replace(" ", "");
        String[] files = arg.split(",");
        myModel2.clear();
        for (String fil : files) {
            myModel2.addElement(fil);
        }
    }

    public void fillList(DefaultListModel<String> myModel, Path path) throws IOException {
        this.serverPath = path;
        List<String> list = downloadFileList();
        myModel.clear();
        myModel.addElement("...");
        for (String filename : list) {
            myModel.addElement(filename);
        }
    }

    public List<String> downloadFileList() throws IOException {
        List<String> list = new ArrayList<String>();

        try {
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            StringBuilder sb = new StringBuilder();
            out.write(("list-files\n" + serverPath + "\n").getBytes(StandardCharsets.UTF_8));
            while (true) {
                byte[] buffer = new byte[512];
                int size = in.read(buffer);
                sb.append(new String(buffer, 0, size));
                if (sb.toString().endsWith("end")) {
                    break;
                }
            }
            if (sb.toString().split("\n")[0].equals("false")) {
                String[] p = serverPath.toString().split("\\\\");
                if (p.length > 1) {
                    String pah = "";
                    for (int i = 0; i < p.length - 1; i++) {
                        pah += p[i] + "\\";
                    }

                    serverPath = Paths.get(pah);
                    sb.delete(0, sb.toString().split("\n")[0].length() + 1);
                }
            }
            out.flush();

            String fileString = sb.substring(0, sb.toString().length() - 4);
            list = Arrays.asList(fileString.split("\n"));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }
}
