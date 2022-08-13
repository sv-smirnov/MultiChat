import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public interface AuthService {
    void start() throws SQLException;
    String getNickByLoginPass(String login, String pass);
    void stop();
    void changeNick(String oldNick, String newNick) throws SQLException ;
}

class BaseAuthService implements AuthService {
    private Connection connection;
    private Statement statement;

    private class Entry {
        private String login;
        private String pass;
        private String nick;

        public Entry(String login, String pass, String nick) {
            this.login = login;
            this.pass = pass;
            this.nick = nick;
        }
    }

    private List<Entry> entries;

    @Override
    public void start() throws SQLException{
        MyServer.LOGGER.info("Сервис аутентификации запущен");
//        System.out.println("Сервис аутентификации запущен");
        connection = DriverManager.getConnection("jdbc:sqlite:users.db");
        statement = connection.createStatement();
        entries = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery("SELECT * FROM users;")) {
            while (rs.next()) {
                entries.add(new Entry(rs.getString("login"), rs.getString("password"), rs.getString("nickname")));
            }
        }
    }

    @Override
    public void stop() {
        MyServer.LOGGER.info("Сервис аутентификации остановлен");
//        System.out.println("Сервис аутентификации остановлен");
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    public BaseAuthService() throws SQLException {
//        entries = new ArrayList<>();
//        try (ResultSet rs = statement.executeQuery("SELECT * FROM users;")) {
//            while (rs.next()) {
//                entries.add(new Entry(rs.getString("login"), rs.getString("password"), rs.getString("nickname")));
//            }
//        }
    }


    @Override
    public String getNickByLoginPass(String login, String pass) {
        for (Entry o : entries) {
            if (o.login.equals(login) && o.pass.equals(pass)) return o.nick;
        }
        return null;
    }
    @Override
    public void changeNick (String oldNick, String newNick) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("UPDATE users SET nickname = ? WHERE nickname = ?");
        ps.setString(1, newNick);
        ps.setString(2, oldNick);
        int rs = ps.executeUpdate();
        System.out.println(rs);
        System.out.println(ps);
    }
}

