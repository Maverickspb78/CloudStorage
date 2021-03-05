package server;

import java.io.*;
import java.net.Socket;

/**
 * Обработчик входящих клиентов
 */
public class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {
            out.writeUTF("connected");

            while (true) {
                String command = in.readUTF();
                if ("upload".equals(command)) {
                    upload(in, out);

                } else if ("download".equals(command)) {
                    // TODO: 02.03.2021
                    // realize download
                    download(in, out);

                } else if ("remove".equals(command)) {
                    // TODO: 02.03.2021
                    // realize remove
                    remove(in, out);

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void upload(DataInputStream in, DataOutputStream out) throws IOException {
        try {
            File file = new File("server" + File.separator + in.readUTF());
            if (!file.exists()) {
                file.createNewFile();
            }
            long size = in.readLong();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[256];
            for (int i = 0; i < (size + 255) / 256; i++) { // FIXME
                int read = in.read(buffer);
                fos.write(buffer, 0, read);
            }
            fos.close();
            out.writeUTF("DONE");
        } catch (Exception e) {
            out.writeUTF("ERROR");
        }
    }

    public void download(DataInputStream in, DataOutputStream out) throws IOException{
        try {
            File file = new File("client" + File.separator + in.readUTF());
            if (!file.exists()) {
                file.createNewFile();
            }
            long size = in.readLong();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[256];
            for (int i = 0; i < (size + 255) / 256; i++) { // FIXME
                int read = in.read(buffer);
                fos.write(buffer, 0, read);
            }
            fos.close();
            out.writeUTF("DONE");
        } catch (Exception e) {
            out.writeUTF("ERROR");
        }
    }

    public void remove(DataInputStream in, DataOutputStream out) throws IOException{
        try {
            File file = new File("server" + File.separator + in.readUTF());
            if (file.exists()) {
                if (file.delete()) {
                    out.writeUTF("File deleted from server");
                } else {
                    out.writeUTF("The file is not deleted from the server");
                }
            }

        } catch (Exception e) {
            out.writeUTF("ERROR");
        }
    }


}
