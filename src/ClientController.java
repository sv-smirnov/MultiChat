import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class ClientController {
    Socket socket;
    DataInputStream in;
    DataOutputStream out;
    String filename;

    @FXML
    TextArea chatArea;
    @FXML
    TextArea chatArea2;
    @FXML
    TextField message;
    @FXML
    Label label;
    @FXML
    FlowPane login;
    @FXML
    Button send;
    @FXML
    TextField log;
    @FXML
    TextField pass;

    @FXML
    public void initialize() {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
                                }
        catch (IOException e) {
            chatArea.appendText("*Не удалось подключиться к серверу");
        e.printStackTrace();
        }
    }

    public void btnLogin(javafx.event.ActionEvent actionEvent) {
            setAuthorized(false);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String strFromServer = in.readUTF();
                            if(strFromServer.startsWith("/authok")) {
                                setAuthorized(true);
                                String myNick = strFromServer.split("\\s")[1];
                                chatArea.appendText("*Вы авторизованы как: " + myNick);
                                chatArea.appendText("\n");
// Получаем название файла
                                filename = "history_" + myNick + ".txt";
                                break;
                            }
                            chatArea.appendText(strFromServer + "\n");
                        }
//  Создаем файл и засовываем его содержимое в List
                        File file = new File(filename);
                            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                                String str;
                                List<String> list = new LinkedList<String>();
                                while ((str = reader.readLine()) != null) {
                                    list.add(str);
                                }
//  Смотрим количество записей, и выводим последние 10 на экран
                                int buf = 10;
                                if (list.size()>= buf) {
                                    for (int i = list.size()-buf; i <list.size(); i++) {
                                        chatArea.appendText(list.get(i));
                                        chatArea.appendText("\n");
                                    }}
                                    else {
                                for (int i = 0; i < list.size(); i++) {
                                    chatArea.appendText(list.get(i));
                                    chatArea.appendText("\n");    }
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
// Пишем лог в файл
                        try (FileWriter writer = new FileWriter(filename, true)) {
                            while (true) {
                                String strFromServer = in.readUTF();
                                if (strFromServer.equalsIgnoreCase("/end")) {
                                    break;
                                } else if (strFromServer.startsWith("/clients")) {
                                    String clients = strFromServer.substring("/clients ".length());
                                    chatArea2.setText(clients);
                                } else {
                                    chatArea.appendText(strFromServer);
                                    chatArea.appendText("\n");
                                    writer.write(strFromServer);
                                    writer.write("\n");
                                    writer.flush();
                                }
                            }
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    catch (EOFException e) {
                        chatArea.appendText("*Превышено время ожидания, переподключитесь \n");
                        e.printStackTrace();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            t.setDaemon(true);
            t.start();

        try {
            System.out.println("/auth " + log.getText() + " " + pass.getText());
            out.writeUTF("/auth " + log.getText() + " " + pass.getText());
            log.clear();
            pass.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void btnSend(javafx.event.ActionEvent actionEvent) {
        try {
            String msg = message.getText();
            if (!msg.equals("")) {
            out.writeUTF(msg);
            message.clear();
            message.requestFocus();}
        } catch (Exception e) {
            e.printStackTrace();
        }}

        public void setAuthorized(boolean b) {
        if (b) {
            label.setVisible(false);
            login.setVisible(false);
            chatArea.setVisible(true);
            message.setVisible(true);
            send.setVisible(true);
            chatArea2.setVisible(true);
        }
        else {
            label.setText("Не авторизованый пользователь");
            login.setVisible(true);
            chatArea.setVisible(true);
            chatArea2.setVisible(false);
            message.setVisible(false);
            send.setVisible(false);}
        }
    }



