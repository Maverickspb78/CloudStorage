import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;

public class FileCloudHandler {
    private Path serverPath = Paths.get("server");
    private Path clientPath = Paths.get("c:\\");
    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;
    private long sizeCloud;

    public FileCloudHandler(Path serverPath, Path clientPath, DataInputStream in, DataOutputStream out, Socket socket) {
        this.serverPath = serverPath;
        this.clientPath = clientPath;
        this.in = in;
        this.out = out;
        this.socket = socket;
    }

    public DataInputStream getIn() {
        return in;
    }

    public DataOutputStream getOut() {
        return out;
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

//    скачка файла с сервера
    public String downloadFile(String filename) {
        try {
            File file = new File(clientPath + File.separator + filename);
            if (!file.exists()) {
                file.createNewFile();

                out.write(("download\n" + filename + "\n").getBytes(StandardCharsets.UTF_8));
                out.flush();

                FileOutputStream fos = new FileOutputStream(clientPath + File.separator + filename);
                while (true) {
                    byte[] buffer = new byte[512];
                    int size = in.read(buffer);
                    fos.write(buffer, 0, size);
                    if (in.available() < 1) {
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
// загрузка файла на сервер
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
// удаление файла\папки
    public String remove(String filename, String position) {
        try {
            if (position.equals("server")) {
                File file = new File(serverPath + File.separator + filename);
                if (file.exists()) {
                    String msg = "remove " + filename;
                    out.write(msg.getBytes(StandardCharsets.UTF_8));
                    out.flush();
                    return readMsg(in);

                } else {
                    return "File is not exists";
                }
            } else {
                File file = new File(clientPath + File.separator + filename);
                if (file.exists()) {
                    if (file.isDirectory()) {
                        Path dir = Paths.get(clientPath + File.separator + filename);
                        try {
                            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

                                @Override
                                public FileVisitResult visitFile(Path file,
                                                                 BasicFileAttributes attrs) throws IOException {

                                    System.out.println("Deleting file: " + file);
                                    Files.delete(file);
                                    return CONTINUE;
                                }

                                @Override
                                public FileVisitResult postVisitDirectory(Path dir,
                                                                          IOException exc) throws IOException {

                                    System.out.println("Deleting dir: " + dir);
                                    if (exc == null) {
                                        Files.delete(dir);
                                        return CONTINUE;
                                    } else {
                                        throw exc;
                                    }
                                }

                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        file.delete();
                        System.out.println(clientPath);
                        System.out.println(file.getName());
                        return "File " + filename + " deleted from " + clientPath.toString();
                    }
                } else {
                    return "File is not exists";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Something error";
    }
// создание директории
    public void createFolder(JTextField textArea, String pos) throws IOException {
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
// чтение с сервера сообщения
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
        if ((sbr.toString().startsWith("1")) && (sbr.toString().split("\n")[0].equals("1"))) {
            serverPath = (Path.of(sbr.toString().split("\n")[1]));
            sizeCloud = Long.parseLong(sbr.toString().split("\n")[2]);
            return sbr.substring(0, 1);
        }
        return sbr.substring(0, sbr.toString().length() - 4);
    }

    public long getSizeCloud() {
        return sizeCloud;
    }
}
