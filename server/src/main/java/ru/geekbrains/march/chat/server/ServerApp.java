package ru.geekbrains.march.chat.server;

public class ServerApp {

    public static void main(String[] args) throws Exception {  // Джава комментирует в процессе, когда пушу на GitHub,
        new Server(8189);                               // что исключение никогда не прокидывалось в метод..

    }
}
