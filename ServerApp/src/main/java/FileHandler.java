
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import javax.crypto.spec.PSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.*;

import static java.nio.file.FileVisitResult.CONTINUE;

public class FileHandler extends SimpleChannelInboundHandler<String> {
    private Path serverPath = Path.of("server");
    private DBHandler dbHandler = new DBHandler();
    private long size;

    public Path getServerPath() {
        return serverPath;
    }

    public void setServerPath(Path serverPath) {
        this.serverPath = serverPath;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected: " + ctx.channel().remoteAddress());
    }


    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {


        String filename = "";
        String command = msg.split("\n")[0];

        //Команда на создания листа файлов в папке

        if (command.startsWith("list-files")) {
            serverPath = Path.of(msg.split("\n")[1]);
            File file = new File(serverPath.toString());
            if (Files.isDirectory(Paths.get(serverPath.toString()))) {
                if (file.getName().equals("...")) {
                    file = new File(file.getParent());
                }
                File[] files = file.listFiles();
                StringBuffer sb = new StringBuffer();
                for (File f : files) {
                    sb.append(f.getName() + "\n");
                }

                sb.append("\nend");

                ctx.writeAndFlush(sb.toString());
            } else {
                String[] p = serverPath.toString().split("\\\\");
                if (p.length > 1) {
                    String pah = "";
                    for (int i = 0; i < p.length - 1; i++) {
                        pah += p[i] + "\\";
                    }

                    serverPath = Paths.get(pah);
                    file = new File(serverPath.toString());
                    File[] files = file.listFiles();
                    StringBuffer sb = new StringBuffer();
                    sb.append("false\n");
                    for (File f : files) {
                        sb.append(f.getName() + "\n");
                    }
                    sb.append("end");
                    ctx.writeAndFlush(sb.toString());
                }
            }

            //Команда на регистрация пользователя

        } else if (command.startsWith("reg")) {
            String anser = "" + registration(msg);
            System.out.println("сработала регистрация");
            System.out.println(anser);
            if (anser.equals("1")) {
                anser = "" + auth(msg);
                if (anser.equals("1")) {
                    ctx.writeAndFlush(anser + "\n" + serverPath.toString() + "\nend");
                } else {
                    ctx.writeAndFlush(anser + "\nend");
                }
            } else {
                ctx.writeAndFlush(anser + "\nend");
            }

            //команда на авторизация

        } else if (command.startsWith("auth")) {
            String anser = "" + auth(msg);
            if (anser.equals("1")) {
                ctx.writeAndFlush(anser + "\n" + serverPath.toString() + "\n" + size + "\nend");
            } else {
                ctx.writeAndFlush(anser + "\nend");
            }

            //команда на создании папки

        } else if (command.startsWith("createFolder")) {
            createFolder(msg.split("\n")[1]);
            ctx.writeAndFlush("msg\n+end");

            //команда на удаление

        } else if (command.startsWith("remove")) {
            filename = msg.split(" ")[1];
            remove(ctx, filename);

            //команда на загрузку файла от клиента

        } else if (command.startsWith("upload")) {
            filename = msg.split("\n")[1];
            String lengthSize = msg.split("\n")[2];

            int length = command.length() + filename.length() + 2;
            System.out.println(length);
            upload(ctx, msg, length);

//            команда на загрузку файла клиенту

        } else if (command.startsWith("download")) {
            filename = msg.split("\n")[1];
            download(ctx, filename);
        } else if (command.startsWith("delAccount")) {
            if (dbHandler.delAccount()) {
                removeAcc(ctx);
                ctx.writeAndFlush("account delete\nend");

            }
            ctx.writeAndFlush("error\nend");

//            команда на смену пароля

        } else if (command.startsWith("changePass")) {
            System.out.println("changePass");
            String newPass = msg.split("\n")[1];
            if (dbHandler.changePass(newPass)) {
                ctx.writeAndFlush("password changed\nend");
            } else {
                ctx.writeAndFlush("password not changed\nend");
            }

        } else {
            System.out.println("Chanel closed");
            ctx.channel().closeFuture();
            ctx.channel().close();
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        System.out.println("Client disconnected: " + ctx.channel().remoteAddress());
    }

    //Авторизация
    private int auth(String msg) throws SQLException, IOException {
        int b = 0;
        String login = msg.split("\n")[1];
        String pass = msg.split("\n")[2];
        String query = "SELECT * FROM Auth WHERE login ='" + login + "' and password = '" + pass + "'";
        b = dbHandler.auth(query, login, pass);
        setServerPath(dbHandler.getServerPath());
        size = sizeCloud(dbHandler.getServerPath());
        System.out.println("размер: " + size);
        return b;
    }

//регистрация
    public int registration(String msg) {
        String login = msg.split("\n")[1];
        String pass = msg.split("\n")[2];
        String query = "SELECT * FROM Auth WHERE login ='" + login + "' and password = '" + pass + "'";
        String query1 = "INSERT INTO Auth (login,password,already,startFolder) VALUES ('" + login + "','" + pass + "',0,'" + login + "')";
        int b = 0;
        b = dbHandler.registration(query, query1, login, pass);
        return b;
    }

// создание папки
    private void createFolder(String dirName) throws IOException {
        File file = new File(serverPath + File.separator + dirName);
        if (!file.exists()) {
            Files.createDirectory(file.toPath());
        }
    }

// удаление фала\папок
    public void remove(ChannelHandlerContext ctx, String filename) throws IOException {
        try {
            File file = new File(serverPath + File.separator + filename);

            if (file.exists()) {
                if (file.isDirectory()) {
                    Path dir = Paths.get(serverPath + File.separator + filename);
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
                    ctx.writeAndFlush("The file " + filename + " is not deleted from the server\nend");
                }
                if (file.delete()) {
                    ctx.writeAndFlush("File " + filename + " deleted from server\nend");
                } else {
//                    ctx.writeAndFlush("The file " + filename + " is not deleted from the server\nend");
                }
            }

        } catch (Exception e) {
            ctx.writeAndFlush("ERROR\nend".getBytes(StandardCharsets.UTF_8));
        }
    }
// загрузка файла на сервер
    public void upload(ChannelHandlerContext ctx, String msg, int length) throws IOException {


        String filename = msg.split("\n")[1];
        try {

            File file = new File(serverPath + File.separator + filename);

            if (!file.exists()) {
                file.createNewFile();
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
                bufferedWriter.write(msg.substring(length));
                bufferedWriter.close();
                ctx.writeAndFlush("File " + filename + " coped from server\nend");
            } else {
                ctx.writeAndFlush("File " + filename + " already exists\nend");
            }
        } catch (Exception e) {
            ctx.writeAndFlush("ERROR\nend");
        }
    }
// загрузка файла клиенту
    public void download(ChannelHandlerContext ctx, String filename) throws IOException {
        try {
            File file = new File(serverPath + File.separator + filename);
            if (file.exists()) {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

                String m = "";
                while (bufferedReader.ready()) {
                    m = m + bufferedReader.readLine() + "\n";
                }
                System.out.println(m);
                ctx.writeAndFlush(m);
                ctx.writeAndFlush("File " + filename + " coped from server\nend");
                bufferedReader.close();

            } else {
                ctx.writeAndFlush("File " + filename + " already exists\nend");
            }
        } catch (Exception e) {
            ctx.writeAndFlush("ERROR\n");
        }


    }
// удаление директории и аккаунта
    public void removeAcc(ChannelHandlerContext ctx) throws IOException {
        Path dir = Paths.get(dbHandler.getServerPath().toString());

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
    }
//  размер файлов в облаке
    public long sizeCloud(Path serverPath) throws IOException {
        Path folder = (serverPath);
        return Files.walk(folder)
                .map(Path::toFile)
                .filter(File::isFile)
                .mapToLong(File::length)
                .sum();
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


}


