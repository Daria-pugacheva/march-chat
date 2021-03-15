package ru.geekbrains.march.chat.server;

public class ServerApp {

    public static void main(String[] args) throws Exception{  // Джава ругается, что исключение
        new Server(8189);                               // никогда не прокидывалось в метод..

    }
}
