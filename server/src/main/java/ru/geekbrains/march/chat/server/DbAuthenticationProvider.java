package ru.geekbrains.march.chat.server;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sqlite.core.DB;

import java.sql.*;

public class DbAuthenticationProvider implements AuthenticationProvider {

    private DbConnection dbConnection;
    private static final Logger LOGGER = LogManager.getLogger(DbAuthenticationProvider.class);

    @Override
    public void init() {
        dbConnection = new DbConnection();

    }


    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        String query = String.format("select nickname from users where login = '%s' and password = '%s';", login,password);
        try (ResultSet rs = dbConnection.getStmt().executeQuery(query)){
            if(rs.next()){
                return rs.getString("nickname");
            }
        }catch (SQLException e){
            //e.printStackTrace();
            LOGGER.throwing(Level.FATAL, e); // создали логгер
        }
        return null;
    }

    @Override
    public void changeNickname(String oldNickname, String newNickname) {
        String query = String.format("update users set nickname = '%s' where nickname = '%s';", newNickname,oldNickname);//в записи урока здесь перепутаны месами старый и новый ник.
        try {
            dbConnection.getStmt().executeQuery(query);
        }catch (SQLException e){
            //e.printStackTrace();
            LOGGER.throwing(Level.FATAL, e); // создали логгер
        }
    }

    @Override /// это проверка для случая, когда меняем ник (не заходим под логином/паролем, а именно меняем)
    public boolean isNickBusy(String nickname) {
        String query = String.format("select id from users where nickname = '%s';", nickname);
        try (ResultSet rs = dbConnection.getStmt().executeQuery(query)){
            if(rs.next()){
                return true;
            }
        }catch (SQLException e){
            //e.printStackTrace();
            LOGGER.throwing(Level.FATAL, e); // создали логгер
        }
        return false;
    }

    @Override
    public void shutdown() {
        dbConnection.close();

    }
}


/////Мой вариант реализации оставляю пока в закомментированном виде.

//    private static Connection connection;
//    private static Statement stmt;
//
//    // сам принцип работы с PreparedStatement понятен, но конкретно в этом коде, кажется более лаконичной
//    // реализация просто Statement (мы ведь в ResultSet складываем итог сортировки)
//
//    @Override
//    public String getNicknameByLoginAndPassword(String login, String password) {
//        try (ResultSet rs = stmt.executeQuery("select nickname from nicknameDirectory where (login = '" + login +
//                "') and (password = '" + password + "');")) {
//            if (rs.next()) {
//                return rs.getString(1);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//
//    @Override /// я его все-таки переопределила. Но также просто через Statement
//    public void changeNickname(String oldNickname, String newNickname) {
//        try (ResultSet res = stmt.executeQuery("select id from nicknameDirectory where nickname = '" +
//                oldNickname + "';")) {
//            if (res.next()) {
//                int idForChangeNick = res.getInt(1);
//                stmt.executeUpdate("update nicknameDirectory set nickname = '" + newNickname + "' where id ="
//                        + idForChangeNick + ";");
//
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void connect() {
//        try {
//            Class.forName("org.sqlite.JDBC");
//            connection = DriverManager.getConnection("jdbc:sqlite:nicknameDirectory.db");
//            stmt = connection.createStatement();
//        } catch (ClassNotFoundException | SQLException e) {
//            e.printStackTrace();
//            throw new RuntimeException("Невозмоно подключиться к БД");
//        }
//
//    }
//
//    public void disconnect() {
//        if (stmt != null) {
//            try {
//                stmt.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//
//        if (connection != null) {
//            try {
//                connection.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//    }


