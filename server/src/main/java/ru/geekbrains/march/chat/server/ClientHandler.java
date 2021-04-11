package ru.geekbrains.march.chat.server;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Locale;

public class ClientHandler {

    private String username;
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class); // создали логгер

    public String getUsername() {
        return username;
    }


    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());

        new Thread(() -> {
        ///server.getExecutorService().execute(()-> { // БУДЕТ ЗАПУСКАТЬСЯ ПОТОМ ИЗ ПУЛА ПОТОКОВ ПО НЕОБХОДИМОСТИ
            try {
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith("/login")) {
                        String[] tokens = msg.split("\\s+");
                        if (tokens.length != 3) {
                            sendMessage("/login_failed Введите логин и пароль");
                            LOGGER.error("Авторизация не удалась. Введите логин и пароль");
                            continue;
                        }
                        String login = tokens[1];
                        String password = tokens[2];

                        String userNickname = server.getAuthenticationProvider().getNicknameByLoginAndPassword(login, password);
                        //String userNickname = server.getDatabaseAuthenticationProvider().getNicknameByLoginAndPassword(login,password); //- моя старая реализация


                        if (userNickname == null) {
                            sendMessage("/login_failed Введен некорректный логин/пароль");
                            LOGGER.error("Авторизация не удалась. Введен некорректный логин/пароль");
                            continue;
                        }
                        if (server.isUserOnline(userNickname)) {
                            sendMessage("/login_failed Учетная запись занята другим пользователем");
                            LOGGER.error("Авторизация не удалась. Учетная запись занята другим пользователем");
                            continue;
                        }
                        username = userNickname;
                        //sendMessage("/login_ok " + username); // было так до написания истории
                        sendMessage("/login_ok " + login + " " + username);
                        server.subscribe(this);
                        break;
                    }
                }

                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith("/")) {
                        executeCommand(msg);
                        continue;
                    }
                    server.broadcastMessage(username + ": " + msg);
                    LOGGER.info("Клиент " + username + " прислал сообщение: " + msg);
                }
            } catch (IOException e) {
                //e.printStackTrace(); // заменяем на логгер
                LOGGER.throwing(Level.FATAL,e);
            } finally {
                disconnect();
            }
            }).start();
       /// }); // У НАС УЖЕ ИСПОЛНЕНИЕ ЧЕРЕЗ ПУЛ ПОТОКОВ, ТАК ЧТО НИКАКОЙ СТАРТ НЕ НУЖЕН
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            LOGGER.throwing(Level.FATAL,e); // Добавили логгер
            disconnect();
        }
    }

    private void executeCommand(String cmd) {
        if (cmd.startsWith("/w ")) {
            String[] tokens = cmd.split("\\s+", 3);
            if (tokens.length !=3){
                sendMessage("Server: Введена некорректная команда");
                LOGGER.error("Введена некорректная команда при отправке личного сообщения"); // Добавили логгирование некорректной команды
                return;
            }
            server.sendPrivateMessage(this, tokens[1], tokens[2]);
            return;
        }
        if (cmd.startsWith("/change_nick ")){
            String [] tokens = cmd.split("\\s+");
            if (tokens.length !=2){
                sendMessage("Server: Введена некорректная команда");
                LOGGER.error("Введена некорректная команда при смене ника"); // Добавили логгирование некорректной команды
                return;
            }
            String newNickname = tokens[1];
            if ((server.getAuthenticationProvider().isNickBusy(newNickname))){
                sendMessage("Server: Такой никнейм занят");
                LOGGER.error("Ошибка при смене ника - такой никнейм занят"); // Добавили логгирование некорректной команды
                return;
            }
            server.getAuthenticationProvider().changeNickname(username, newNickname);
            //server.getDatabaseAuthenticationProvider().changeNickname(username, newNickname); //- моя старая реализация
            username = newNickname;
            sendMessage("Server: Ваш никнейм изменен на " + newNickname);
            LOGGER.info("Ваш никнейм изменен на " + newNickname); // логирование смены ника
            server.broadcastClientsList();
        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
