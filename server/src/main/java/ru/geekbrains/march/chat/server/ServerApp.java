package ru.geekbrains.march.chat.server;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public class ServerApp {

    public static void main(String[] args) throws Exception {
        new Server(8189);

    }
}

