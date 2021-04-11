package ru.geekbrains.march.chat.client;

import java.io.*;
import java.nio.charset.StandardCharsets;

//todo доделать буферизацию и преенос строк в истории

public class HistoryManager {

    private String login;
    private OutputStream out;

    public void init (String login){
        try {
            this.login = login;
            this.out = new FileOutputStream(getFilename(), true);
        } catch (IOException e) {
            throw new RuntimeException("Проблема при работе с историей");
        }
    }

    public void write (String message){
        try {
            out.write(message.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Проблема при работе с историей");
        }

    }

    public String load(){
        StringBuilder builder = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new FileReader(getFilename()))){
            String str;
            while ((str = in.readLine()) != null){
                builder.append(str).append("\n");
            }
            return builder.toString();
        }catch (IOException e){
            throw new RuntimeException("Проблема при работе с историей");
        }
    }

    public void close(){
        login=null;
        if(out != null){
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getFilename (){
        return "history " + login + ".txt";
    }

}
