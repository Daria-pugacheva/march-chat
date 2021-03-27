package ru.geekbrains.march.chat.server;

import java.sql.*;

public class DatabaseAuthenticationProvider implements AuthenticationProvider {

    private static Connection connection;
    private static Statement stmt;

    // сам принцип работы с PreparedStatement понятен, но конкретно в этом коде, кажется более лаконичной
    // реализация просто Statement (мы ведь в ResultSet складываем итог сортировки)

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        try (ResultSet rs = stmt.executeQuery("select nickname from nicknameDirectory where (login = '" + login +
                "') and (password = '" + password + "');")) {
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override /// я его все-таки переопределила. Но также просто через Statement
    public void changeNickname(String oldNickname, String newNickname) {
        try (ResultSet res = stmt.executeQuery("select id from nicknameDirectory where nickname = '" +
                oldNickname + "';")) {
            if (res.next()) {
                int idForChangeNick = res.getInt(1);
                stmt.executeUpdate("update nicknameDirectory set nickname = '" + newNickname + "' where id ="
                        + idForChangeNick + ";");

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:nicknameDirectory.db");
            stmt = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Невозмоно подключиться к БД");
        }

    }

    public void disconnect() {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
