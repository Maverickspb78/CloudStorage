import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileCloudHandler {
    private Path serverPath = Paths.get("server");
    private Path clientPath = Paths.get("c:\\");
    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;

    public FileCloudHandler(Path serverPath, Path clientPath, DataInputStream in, DataOutputStream out, Socket socket) {
        this.serverPath = serverPath;
        this.clientPath = clientPath;
        this.in = in;
        this.out = out;
        this.socket = socket;
    }



    public Path getServerPath() {
        return serverPath;
    }

    public void setServerPath(Path serverPath) {
        this.serverPath = serverPath;
    }

    public Path getClientPath() {
        return clientPath;
    }

    public void setClientPath(Path clientPath) {
        this.clientPath = clientPath;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setIn(DataInputStream in) {
        this.in = in;
    }

    public void setOut(DataOutputStream out) {
        this.out = out;
    }

    public String downloadFile(String filename) {
        try {
            File file = new File(clientPath + File.separator + filename);
            if (!file.exists()) {
                file.createNewFile();

                out.write(("download\n" + filename+"\n").getBytes(StandardCharsets.UTF_8));
                out.flush();

                FileOutputStream fos = new FileOutputStream(clientPath + File.separator + filename);
                while (true) {
                    byte[] buffer = new byte[512];
                    int size = in.read(buffer);
                    fos.write(buffer,0,size);
                    if (in.available()<1) {
                        break;
                    }
                }
                fos.close();

                return readMsg(in);
            } else {
                return "File is not exists";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Something error";
    }

    public String sendFile(String filename) {
        try {
            File file = new File(clientPath + File.separator + filename);
            if (file.exists()) {
                String msg = ("upload\n" + filename + "\n");
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream inputStream = new BufferedInputStream(fis, 512);
                long size = file.length();
                out.write(msg.getBytes());
                while ((inputStream.available() > 0)) {
                    out.write(inputStream.readAllBytes());
                }
                out.flush();
                return readMsg(in);

            } else {
                return "File is not exists";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Something error";
    }

    public String remove(String filename, String position){
        try {
            if (position.equals("server")){
                File file = new File(serverPath + File.separator + filename);
                if (file.exists()) {
                    String msg = "remove " + filename;
                    out.write(msg.getBytes(StandardCharsets.UTF_8));
                    out.flush();
                    return readMsg(in);

                } else {
                    return "File is not exists";
                }
            }
            else {
                File file = new File(clientPath + File.separator + filename);
                if (file.exists()) {
                    file.delete();
                    System.out.println(clientPath);
                    System.out.println(file.getName());
                    return "File " + filename + " deleted from " + clientPath.toString();
                } else {
                    return "File is not exists";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Something error";
    }

    public void createFolder(JTextArea textArea, String pos) throws IOException {
        if (pos.equals("right")) {
            if (!new File(clientPath + File.separator + textArea.getText()).exists()) {
                Files.createDirectory(Paths.get(clientPath + (File.separator + textArea.getText())));
            }
        } else if (pos.equals("left")) {
            String m = "createFolder\n" + textArea.getText();
            out.write(m.getBytes(StandardCharsets.UTF_8));
            out.flush();
            readMsg(in);
        } else System.out.println("wrong null name folder");
    }

    public String readMsg(DataInputStream in) throws IOException {
        StringBuilder sbr = new StringBuilder();
        while (true) {
            byte[] buffer = new byte[512];
            int size = in.read(buffer);
            sbr.append(new String(buffer, 0, size));
            if (sbr.toString().endsWith("end")) {
                break;
            }
        }
        System.out.println(sbr.toString());


        if ((sbr.toString().startsWith("1"))&&(sbr.toString().split("\n")[0].equals("1"))){
            serverPath = (Path.of(sbr.substring(2,sbr.toString().length()-4)));
            System.out.println(sbr.length());



            System.out.println(sbr.substring((2+sbr.toString().split("\n")[1].length()),sbr.toString().length()-4));
            return sbr.substring(0,1);
        }

        return sbr.substring(0, sbr.toString().length() - 4);
    }
}
