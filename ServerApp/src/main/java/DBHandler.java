import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

public class DBHandler {
    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    Path serverPath;
    String login = "";
    String pass = "";
    int id = 0;



    public DBHandler() {
        try {
            Class.forName("org.sqlite.JDBC");
            ///////////////////////////// Write path to DB /////////////////////////////
            String url = "jdbc:sqlite:SCloudDB";
            ///////////////////////////////////////////////////////////////////////////
            connection = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int auth(String query, String login, String pass) throws SQLException, IOException {
        this.login = login;
        this.pass = pass;
        int b =0;
        statement = connection.createStatement();
        resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
            setId(resultSet.getInt(1));
            serverPath = Path.of(resultSet.getString("startFolder"));
            if ((resultSet.getString("login").equals(login))
                    && (resultSet.getString("password").equals(pass))
                    && (resultSet.getInt("already")) == 0) {
                b = 1;
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

    public int registration(String query,String query1, String login, String pass) {
        this.login = login;
        this.pass = pass;
        int b = 0;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            if (!resultSet.next()) {
                resultSet.close();
                System.out.println("work if !resultSet.next");
                PreparedStatement preparedStatement = connection.prepareStatement(query1);
                preparedStatement.execute();
                preparedStatement.close();
                serverPath = Path.of(login);
                b = 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return b;
    }

    public Path getServerPath() {
        return serverPath;
    }

    public boolean changePass(String newPass) throws SQLException {
        String query = "UPDATE Auth set password =" +newPass+" WHERE id = "+id;
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        if (preparedStatement.executeUpdate() ==1){
            return true;
        }
        preparedStatement.close();

        return false;
    }

    public boolean delAccount() throws SQLException {
        String query = "DELETE FROM Auth  WHERE id = "+id;
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        if (preparedStatement.executeUpdate() ==1){
            return true;
        }
        preparedStatement.close();

        return false;
    }
}

