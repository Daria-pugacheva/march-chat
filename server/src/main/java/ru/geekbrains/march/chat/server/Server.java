package ru.geekbrains.march.chat.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private int port;
    private List<ClientHandler> clients;
    private AuthenticationProvider authenticationProvider;
   // private DbAuthenticationProvider databaseAuthenticationProvider;  - моя старая реализация
    private ExecutorService executorService; // У СЕРВЕРА ЗАДАЕМ ПОЛЕ ПУЛА ПОТОКОВ, КОТОРЫЙ БУДЕМ ИСПОЛЬЗОВАТЬ

    public AuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    public ExecutorService getExecutorService(){ return executorService; } //ГЕТТЕР, ЧТОБЫ МОЖНО БЫЛО ОБРАЩАТЬСЯ

//    public DbAuthenticationProvider getDatabaseAuthenticationProvider() {
//        return databaseAuthenticationProvider;
//    }  - моя старая реализация

    public Server(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
        //this.authenticationProvider = new InMemoryAuthenticationProvider();
        this.authenticationProvider = new DbAuthenticationProvider();
        this.authenticationProvider.init(); // ВОПРОС: А зачем здесь this?
        //this.databaseAuthenticationProvider = new DbAuthenticationProvider();
        //databaseAuthenticationProvider.connect();
        this.executorService = Executors.newCachedThreadPool(); //СОЗДАЕМ ПУЛ ПОТОКОВ
            try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port);
            while (true) {
                System.out.println("Ждем нового клиента...");
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
                e.printStackTrace();
            }finally {
                this.authenticationProvider.shutdown(); // ВОПРОС: И здесь тоже зачем this?
                executorService.shutdown(); // ЗАКРЫВАЕМ ПУЛ, КОГДА СЕРВЕР ЗАВЕРШАТ РАБОТУ
            }

//        } finally {
//            databaseAuthenticationProvider.disconnect();
//        }

    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastMessage("Клиент " + clientHandler.getUsername() + " вошел в чат.");
        broadcastClientsList();
       // sendHistory(clientHandler); //  при регистрации клиента ему высылается история переписки
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
       // addHistory(message); // при каждой широковещательной рассылке пополняем общую историю

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
//// метод записи истории в файл
//    public void addHistory(String text){
//       try(OutputStreamWriter outWrite = new OutputStreamWriter(new FileOutputStream("chatHistory.txt",true))){
//           outWrite.write(text);
//           outWrite.write("\n");
//        } catch (IOException e){
//            e.printStackTrace();
//        }
//
//    }
//
//    // метода отправки истории из файла подключившемуся клиенту
//
//    public void sendHistory(ClientHandler clientHandler){
//        String historyText = null;
//        try(InputStreamReader inRead = new InputStreamReader(new FileInputStream("chatHistory.txt"))){
//            StringBuilder text = new StringBuilder("ИСТОРИЯ ПЕРЕПИСКИ:\n");
//            int x;
//            while((x=inRead.read()) != -1){
//                text.append((char)x);
//            }
//            historyText = text.toString();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        clientHandler.sendMessage(historyText);
//    }





}
