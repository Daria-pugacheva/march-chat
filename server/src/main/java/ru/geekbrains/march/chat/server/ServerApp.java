package ru.geekbrains.march.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerApp {
    public static void main(String[] args) throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            System.out.println("Сервер запущен на порту 8189. Ожидаем подключения клиента...");
            Socket socket = serverSocket.accept();
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            System.out.println("Клиент подключился");
            //Thread.sleep(3000);    // Можно пояснить по этой строке кода?
            // Мы обратились к классу Thread напрямую и попросили его поспать тут. И
            // т.к. у нас никаких параллельных потоков нет, то поспал наш main?

            while (true) {
                String msg = in.readUTF();
                System.out.println(msg);
                out.writeUTF("ECHO: " + msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
