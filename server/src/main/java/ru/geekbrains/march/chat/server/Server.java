package ru.geekbrains.march.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Server {
    private int port;
    private List<ClientHandler> clients;
    private HashMap<String, String> nickDirectory; // справочник логин/пароль-ник (ниже по заполнению видно,
    // почему типы данных оба строки. Может,  сомнительная оптимизация, но мне так увиделось).

    public Server(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
        this.nickDirectory = new HashMap<>(); // создаем и
        completeDirectory();                // заполняем справочник логин/пароль-ник
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port);
            while (true) {  // при пуше Джава ворчит, что не может быть завершено без проброски исключения... Это норм или что-то упустили?
                System.out.println("Ждем нового клиента...");
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //Заполняем некий постоянный справочник на сервере. Пара логин пароль преобразована в одну строку (например,
    // логин "one"  и пароль "on" превращены в строку "OneON". Перемудрила, наверное...
    // Задать в виде ключа в HashMap массив из двух значений логин/пароль не получилось (ну.. т.е. можно было бы
    // дополнительный свой equals, наверное,  переопределить, но я пошла по более простому для себя пути)
    // Вынесла отдельным методом, чтобы конструктор не засорять.
    public void completeDirectory() {
        nickDirectory.put("oneON", "fox");
        nickDirectory.put("twoTW", "frog");
        nickDirectory.put("threeTH", "fish");
        nickDirectory.put("fourFO", "snake");
        nickDirectory.put("fiveFI", "rabbit");
        nickDirectory.put("sixSI", "cat");
    }

    public boolean giveNick(ClientHandler sender, String key) {  //метод выдачи ника. Его, пожалуй, можно не синхронизировать
        String nickOfKeyFromUser = nickDirectory.getOrDefault(key, "none");
        if (!nickOfKeyFromUser.equals("none")) {
            sender.setUsername(nickOfKeyFromUser); // сделала сэттер, потому что удобно отсюда присваивать имя
            sender.sendMessage("/login_ok " + nickOfKeyFromUser);
            sender.sendMessage("Вам присвоен ник " + nickOfKeyFromUser);
            subscribe(sender);
            return true;
        }
        sender.sendMessage("/login_failed Такой пары логин-пароль не существует. Ник не присвоен.");
        return false;
    }

    //***
    // Здесь и в ClientHandler закомментировано тело метода giveNick(ниже), при котором не отправлялось сообщение
    // про присвоение ника. При этом ник присваивался. Но самое первое сообщение из текстового поля клиента улетало
    // неизвестно куда, в рассылку не попадало и в консоли выводилось сообщение, что ошибка и в ClientHandler и в
    // Controller взникает на моменте чтения входящих потоков. При этом второе и последущюие сообщения прекрасно
    // отправлялись, везде отображались и все замечательно работало... Не могу понять, в чем дело...
//        if (nickDirectory.containsKey(key)) {
//            sender.setUsername(nickDirectory.get(key));
//            sender.sendMessage("/login_ok " + nickDirectory.get(key));
//            sender.sendMessage("Вам присвоен ник " + nickDirectory.get(key)); - это не отправляется
//            subscribe(sender);
//        }
//        throw new NullPointerException();
//    }


    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastMessage("Клиент " + clientHandler.getUsername() + " вошел в чат.");
        broadcastClientsList();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastMessage("Клиент " + clientHandler.getUsername() + " вышел из чата.");
        broadcastClientsList();
    }

    public synchronized void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendMessage(message);
        }

    }

    public synchronized void sendPrivateMessage(ClientHandler sender, String receiverUsername, String message) {
        for (ClientHandler c : clients) {
            if (c.getUsername().equals(receiverUsername)) {
                c.sendMessage("От " + sender.getUsername() + " сообщение: " + message);
                sender.sendMessage("Пользователю " + receiverUsername + " сообщение: " + message);
                return;
            }
        }
        sender.sendMessage("Невозможно отправить сообщение " + receiverUsername + ". Такого пользователя нет в сети.");

    }

    public synchronized boolean isUserOnline(String username) {
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void broadcastClientsList() {
        StringBuilder stringBuilder = new StringBuilder("/clients_list ");
        for (ClientHandler c : clients) {
            stringBuilder.append(c.getUsername()).append(" ");
        }
        stringBuilder.setLength(stringBuilder.length() - 1);
        String clientsList = stringBuilder.toString();
        for (ClientHandler c : clients) {  // тут же можно просто одной строкой сделать
            c.sendMessage(clientsList);  // broadcastMessage (clientsList), не?
        }
    }

}
