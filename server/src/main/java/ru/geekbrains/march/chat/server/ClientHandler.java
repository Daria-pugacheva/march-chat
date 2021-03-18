package ru.geekbrains.march.chat.server;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String name) {  // чтобы из сервера присвоить клиентХэндлеру имя
        this.username = name;

    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());

        new Thread(() -> {
            try {
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith("/login")) {
                        String usernameFromLogin = msg.split("\\s")[1];
                        if (server.isUserOnline(usernameFromLogin)) {
                            sendMessage("/login_failed Current nickname is already used");
                            continue;
                        }
                        username = usernameFromLogin;
                        sendMessage("/login_ok " + username);
                        server.subscribe(this);
                        break;
                    }
                    if (msg.startsWith("/logPass ")) {  // добавился вариант авторизации через логин/пароль
                        StringBuilder makeKey = new StringBuilder(msg.split("\\s", 3)[1]);
                        makeKey.append(msg.split("\\s", 3)[2].toUpperCase(Locale.ROOT));
                        String key = makeKey.toString();
                        if (!server.giveNick(this, key)) {
                            continue;
                        }
                        break;
                    }
                }

                //*** Ниже альтернативный кусок кода с 52-й строки, который паршиво работал. Подробно
                // про проблемы его выполнении я расписала в классе Server
                //  try{
                //       server.giveNick(this, key);
                //       sendMessage("Вам присвоет ник " + username); //это сообщение не отправляется
                //       break;
                //      } catch (NullPointerException e){
                //          sendMessage("/login_failed Такая пара логин/пароль отсутствует");
                //          continue;
                //      }
                //      break;
                //   }
                //  break; - и при этой кривой реализации также для выхода из цикла авторизации нужен был этот по сути дублирующий break...
                // }


                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith("/")) {
                        executeCommand(msg);
                        continue;
                    }
                    server.broadcastMessage(username + ": " + msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            disconnect();
        }
    }

    private void executeCommand(String cmd) {
        if (cmd.startsWith("/w ")) {
            String[] tokens = cmd.split("\\s", 3);
            server.sendPrivateMessage(this, tokens[1], tokens[2]);
            return;
        }
        //if(cmd.equals("/exit")){  тут, соответственно, и не нужен этот код, т.к. при отключении
        //disconnect();         входящего потока от клиента, автоматом ловится исключение
        // return;              и проиходит дисконнект... Как-то так?
        // }
        if (cmd.startsWith("/change_nick ")) {  // смена ника
            String newNick = cmd.split("\\s")[1]; // выцепили новый ник и всем сообщаем:
            server.broadcastMessage("Пользователь " + username + " поменял ник на " + newNick);
            username = newNick; // меняем имя пользователя нашего ClientHandler
            sendMessage("/new_nick " + newNick); // отправляем клиенту инфо про его новое имя
            server.broadcastClientsList(); // обновляем ListView
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
