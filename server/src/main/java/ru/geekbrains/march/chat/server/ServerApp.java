package ru.geekbrains.march.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ServerApp {

    public static void main(String[] args) throws Exception {
        int msgNumber=0;
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            System.out.println("Сервер запущен на порту 8189. Ожидаем подключения клиента...");
            Socket socket = serverSocket.accept();
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            System.out.println("Клиент подключился");
            //Thread.sleep(3000);    // Можно пояснить по этой строке кода (была вначале, потом убрали)?
            // Мы обратились к классу Thread напрямую и попросили его поспать тут. И
            // т.к. у нас никаких параллельных потоков нет, то поспал наш main, так?

            while (true) {
                String msg = in.readUTF();
                if (msg.equals("/stat")) {
                    System.out.println("Колличество сообщений клиентов: " + msgNumber);
                    out.writeUTF("Колличество сообщений клиентов: " + msgNumber);
                } else {
                    System.out.println(msg);
                    out.writeUTF("ECHO: " + msg);
                    msgNumber++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
