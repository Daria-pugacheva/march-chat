package ru.geekbrains.march.chat.client;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    @FXML
    TextField msgField, usernameField, loginField, passwordField;

    @FXML
    TextArea msgArea;

    @FXML
    HBox loginPanel, msgPanel, exitPanel, logPassPanel;

    @FXML
    ListView<String> clientsList;

    private String username;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public void setUsername(String username) {
        this.username = username;
        if (username != null) {
            usernameField.clear();
            loginPanel.setVisible(false);
            loginPanel.setManaged(false);
            logPassPanel.setVisible(false);
            logPassPanel.setManaged(false);
            msgPanel.setVisible(true);
            msgPanel.setManaged(true);
            clientsList.setVisible(true);
            clientsList.setManaged(true);
            exitPanel.setVisible(true);
            exitPanel.setManaged(true);
        } else {
            loginPanel.setVisible(true);
            loginPanel.setManaged(true);
            logPassPanel.setVisible(true);
            logPassPanel.setManaged(true);
            msgPanel.setVisible(false);
            msgPanel.setManaged(false);
            clientsList.setVisible(false);
            clientsList.setManaged(false);
            exitPanel.setVisible(false);
            exitPanel.setManaged(false);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUsername(null);
    }

    public void login() {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        if (usernameField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Имя пользователя не может быть пустым", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        try {
            out.writeUTF("/login " + usernameField.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void logOut() {
        if (socket != null || !socket.isClosed()) { // А когда все-таки какое из этих условий надо проверять?(не всегда ведь оба. А тут еще при пуше Джава предупреждает про возможный NullPointerException))
//            try {                         вот эта вся перекличка с сервером, получается,
//                out.writeUTF("/exit");    и не нужна, т.к. клиент отключается, а сервер
//            } catch (IOException e) {     ловит исключение из-за пустого входящего потока
//                e.printStackTrace();      и тоже дисконнектится?... Так ведь?
//            }                             Сначала я все это трепетно прописала, а потом показалось, что лишнее.
            disconnect();
        }
    }


    public void receiveNick() {  // отправляем по нажанию на кнопку логин и пароль на сервер, чтобы получить ник
        if (socket == null || socket.isClosed()) {
            connect();
        }
        String login = loginField.getText();
        String password = passwordField.getText();
        try {
            out.writeUTF("/logPass " + login + " " + password);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось отправить логин и пароль", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void connect() {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            Thread t = new Thread(() -> {
                try {
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/login_ok")) {
                            setUsername(msg.split("\\s")[1]);
                            break;
                        }
                        if (msg.startsWith("/login_failed")) {
                            String cause = msg.split("\\s", 2)[1];
                            msgArea.appendText(cause + "\n");
                        }

                    }

                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/")) {
                            if (msg.startsWith("/new_nick ")) { // запоминаем новый ник
                                setUsername(msg.split("\\s")[1]);
                                continue;
                            }
                            if (msg.startsWith("/clients_list ")) {
                                String[] tokens = msg.split("\\s");

                                Platform.runLater(() -> {
                                    clientsList.getItems().clear();
                                    for (int i = 1; i < tokens.length; i++) {
                                        clientsList.getItems().add(tokens[i]);
                                    }
                                });
                            }
                            continue;
                        }
                        msgArea.appendText(msg + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    disconnect();
                }
            });
            t.start();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно подключиться к серверу", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void sendMsg() {
        try {
            if (!msgField.getText().isEmpty()) {
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
