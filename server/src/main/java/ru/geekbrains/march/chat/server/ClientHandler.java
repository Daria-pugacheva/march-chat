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
                        String [] tokens = msg.split("\\s+");
                        if (tokens.length!=3){
                            sendMessage("/login_failed Введите логин и пароль");
                            continue;
                        }
                        String login = tokens[1];
                        String password = tokens[2];

                        //String userNickname = server.getAuthenticationProvider().getNicknameByLoginAndPassword(login,password);
                        String userNickname = server.getDatabaseAuthenticationProvider().getNicknameByLoginAndPassword(login,password);


                        if (userNickname==null){
                            sendMessage("/login_failed Введен некорректный логин/пароль");
                            continue;
                        }
                        if (server.isUserOnline(userNickname)) {
                            sendMessage("/login_failed Учетная запись занята другим пользователем");
                            continue;
                        }
                        username = userNickname;
                        sendMessage("/login_ok " + username);
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
            String[] tokens = cmd.split("\\s+", 3);
            if (tokens.length !=3){
                sendMessage("Server: Введена некорректная команда");
                return;
            }
            server.sendPrivateMessage(this, tokens[1], tokens[2]);
            return;
        }
        if (cmd.startsWith("/change_nick ")){
            String [] tokens = cmd.split("\\s+");
            if (tokens.length !=2){
                sendMessage("Server: Введена некорректная команда");
                return;
            }
            String newNickname = tokens[1];
            if ((server.isUserOnline(newNickname))){
                sendMessage("Server: Такой никнейм занят");
                return;
            }
            //server.getAuthenticationProvider().changeNickname(username, newNickname);
            server.getDatabaseAuthenticationProvider().changeNickname(username, newNickname);
            username = newNickname;
            sendMessage("Server: Ваш никнейм изменен на " + newNickname);
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
