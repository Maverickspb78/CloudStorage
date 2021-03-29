
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


import java.io.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

public class FileHandler extends SimpleChannelInboundHandler<String> {
    private Path serverPath = null;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected: " + ctx.channel().remoteAddress());
    }


    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {


        String filename = "";
        System.out.println("Message:\n" + msg);
        String command = msg.split("\n")[0];
        if (command.startsWith("list-files")) {
            Path serverPath = Path.of(msg.split("\n")[1]);
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
                sb.append("end");
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

        } else if (command.startsWith("auth")) {
            String anser = "" + auth(msg);
            System.out.println("сработала авторизация");
            System.out.println(anser);
            if (anser.equals("1")) {
                ctx.writeAndFlush(anser + "\n" + serverPath.toString() + "\nend");
            } else {
                ctx.writeAndFlush(anser + "\nend");
            }


        } else if (command.startsWith("remove")) {
            filename = msg.split(" ")[1];
            remove(ctx, filename);
        } else if (command.startsWith("upload")) {
            filename = msg.split("\n")[1];
            int length = command.length() + filename.length() + 2;
            System.out.println(length);
            upload(ctx, msg, length);

        } else if (command.startsWith("download")) {
            filename = msg.split("\n")[1];
            download(ctx, filename);
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

    private int auth(String msg) throws IOException {
        int id = 0;
        String login = msg.split("\n")[1];
        String pass = msg.split("\n")[2];
        String query = "SELECT * FROM Auth WHERE login ='" + login + "' and password = '" + pass + "'";
        Connection connection = null;
        int b = 0;
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:F:\\study\\CloudStorage\\SCloudDB";
            connection = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                id = resultSet.getInt(1);
                serverPath = Path.of(resultSet.getString("startFolder"));
//            System.out.println(id + " path " +serverPath);
                System.out.println(((resultSet.getString("login").equals(login))
                        && (resultSet.getString("password").equals(pass))
                        && (resultSet.getInt("already")) == 0));
                if ((resultSet.getString("login").equals(login))
                        && (resultSet.getString("password").equals(pass))
                        && (resultSet.getInt("already")) == 0) {

//                query = "UPDATE Auth SET already = 1 WHERE id='" + id + "'";
//                System.out.println(query);
                    b = 1;
//                resultSet = statement.executeQuery(query);
                }

            }
//            while (resultSet.next()) {
////				int id = resultSet.getInt("id");
////				String login1 = resultSet.getString("login");
////				String pass1 = resultSet.getString("password");
////				int already = resultSet.getInt("already");
////                startFolder = resultSet.getString("startFolder");
//
////				System.out.println("\n================\n");
////				System.out.println("id: " + id);
////				System.out.println("Name: " + login1);
////				System.out.println("Password: " + pass1);
////				System.out.println("already: " + already);
////				System.out.println("startFolder :" + startFolder);
//            }
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
        File fileAu = new File(serverPath.toString());
        if (!fileAu.exists()) {
            Files.createDirectory(serverPath);
            fileAu = new File(serverPath.toString() + File.separator + "readme.txt");
            if (!fileAu.exists()) {
                fileAu.createNewFile();
            }
        }
        return b;
    }

    public int registration(String msg) {
        String login = msg.split("\n")[1];
        String pass = msg.split("\n")[2];
        String query = "SELECT * FROM Auth WHERE login ='" + login + "' and password = '" + pass + "'";
        String query1 = "INSERT INTO Auth (login,password,already,startFolder) VALUES ('" + login + "','" + pass + "',0,'" + login + "')";
        Connection connection = null;
        int b = 0;
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:F:\\study\\CloudStorage\\SCloudDB";
            connection = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            if (!resultSet.next()) {
                System.out.println("work if !resultSet.next");
                PreparedStatement preparedStatement = connection.prepareStatement(query1);
                preparedStatement.execute();
                b = 1;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return b;
    }


    public void remove(ChannelHandlerContext ctx, String filename) throws IOException {
        try {
            File file = new File("server" + File.separator + filename);
            if (file.exists()) {
                if (file.delete()) {
                    ctx.writeAndFlush("File " + filename + " deleted from server\nend");
                } else {
                    ctx.writeAndFlush("The file " + filename + " is not deleted from the server\nend");
                }
            }

        } catch (Exception e) {
            ctx.writeAndFlush("ERROR\nend".getBytes(StandardCharsets.UTF_8));
        }
    }

//	public static void connect() {
//		Connection conn = null;
//		try {
//			Class.forName("org.sqlite.JDBC");
//			String url = "jdbc:sqlite:F:\\study\\CloudStorage\\SCloudDB";
//			conn = DriverManager.getConnection(url);
//			System.out.println("Connection to SQLite has been established.");
//		} catch (SQLException | ClassNotFoundException e) {
//			System.out.println(e.getMessage());
//		} finally {
//			try {
//				if (conn != null) {
//					conn.close();
//				}
//			} catch (SQLException ex) {
//				System.out.println(ex.getMessage());
//			}
//		}
//	}

    public void upload(ChannelHandlerContext ctx, String msg, int length) throws IOException {


        String filename = msg.split("\n")[1];
        try {

            File file = new File(serverPath + File.separator + filename);

            if (!file.exists()) {
                file.createNewFile();
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
//				msg = ctx.channel().read().alloc().buffer().toString();
//				bufferedWriter.write(msg);
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //cause.printStackTrace();
        ctx.close();
    }


}


