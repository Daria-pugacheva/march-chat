package ru.geekbrains.march.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private String username;
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public String getUsername(){
        return username;
    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());

        new Thread(()->{
            try {
                while (true) {
                    String msg = in.readUTF();
                    if(msg.startsWith("/login")){
                        String usernameFromLogin = msg.split("\\s")[1];
                        if(server.isNickBusy(usernameFromLogin)){
                            sendMessage("/login_failed Current nickname is already used");
                            continue;
                        }
                        username=usernameFromLogin;
                        sendMessage("/login_ok " + username);
                        server.subscribe(this);
                        break;
                    }
                }

                while (true) {
                    String msg = in.readUTF();
                    if(msg.startsWith("/who_am_i")){        //этот запрос, вроде, связан с идентификацией,
                        sendMessage("You are " + username); // но он случается уже в процессе общения.
                        continue;
                    }
                    if(msg.equals("/exit")){
                        sendMessage("/exit");// это служебное сообщение клиенту, чтобы он закрыл сокет
                        disconnect();         // Разрыв соединения происходит. Но окно остается
                        break;                // незакрытым. Т.е. клиент должен заново регистроваться
                    }                         // и "подписываться" в том же окне.

                    // при отправке сообщения типа /w user1 hello,user1  /w - идентификатор вида
                    //служебного сообщения; user1 - ник пользователя; hello, user1 - любой следующий
                    // далее текст для персональной отправки.

                    if(msg.startsWith("/w")){
                        String user = msg.split("\\s")[1];
                        String personalMsg = msg.split("\\s",3)[2];
                        server.targetSendMessage(user,username + ": " + personalMsg);
                        continue;
                    }
                    server.broadcastMessage(username + ": " + msg);
                }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                disconnect();
            }
        }).start();
    }

    public void sendMessage(String message) throws IOException {
        out.writeUTF(message);
    }

    public void disconnect(){
        server.unsubscribe(this);
        if(socket!=null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
