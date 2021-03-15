package ru.geekbrains.march.chat.client;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;


public class Controller {

    @FXML
    TextField msgField, usernameField;

    @FXML
    TextArea msgArea;

    @FXML
    HBox loginPanel, msgPanel;

    private String username;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public void setUsername(String username){
        this.username=username;
        if(username != null){
            // добавила очистку поля, т.к. без нее при очередной регистрации в поле "висит" старый ник
            usernameField.clear();
            loginPanel.setVisible(false);
            loginPanel.setManaged(false);
            msgPanel.setVisible(true);
            msgPanel.setManaged(true);
        }else{
            loginPanel.setVisible(true);
            loginPanel.setManaged(true);
            msgPanel.setVisible(false);
            msgPanel.setManaged(false);
        }
    }

    public void login() {
        if(socket == null || socket.isClosed()){
            connect();
        }
        if (usernameField.getText().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Имя пользователя не может быть пустым",ButtonType.OK);
            alert.showAndWait();
            return;
        }
        try {
            out.writeUTF("/login " + usernameField.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void connect(){
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            Thread t = new Thread(() -> {
                try {
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/login_ok")){
                            setUsername(msg.split("\\s")[1]);
                            break;
                            }
                        if (msg.startsWith("/login_failed")){
                            String cause = msg.split("\\s",2)[1];
                            msgArea.appendText(cause + "\n");
                        }

                        }

                    while (true) {
                        String msg = in.readUTF();
                        if(msg.equals("/exit")){   // закрываем сокет со стороны клиента.
                            disconnect();
                            break;
                        }
                        msgArea.appendText(msg + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    disconnect();
                }
            });
            t.start();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно подключиться к серверу",ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void sendMsg() {
        try {
            if(!msgField.getText().isEmpty()) { // добавила,чтобы пустые сообщения не улетали
                out.writeUTF(msgField.getText());
                msgField.clear();
                msgField.requestFocus();
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалость отправить сообщение", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void disconnect() {
        if (socket != null) {
            setUsername(null);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
