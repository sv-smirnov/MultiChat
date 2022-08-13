import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;

public class ClientHandler {
    private MyServer myServer;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String name;

    public String getName() {
        return name;
    }

    public ClientHandler(MyServer myServer, Socket socket) {
        try {
            this.myServer = myServer;
            this.socket = socket;
            socket.setSoTimeout(60000);
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.name = "";
            myServer.executorService.execute(() -> {
                try {
                    authentication();
                    readMessages();
                }
                catch (IOException e) {
                    MyServer.LOGGER.error(e);
                    e.printStackTrace();
                }
                finally {
                    closeConnection();
                }
            });

//            new Thread(() -> {
//                try {
//                    authentication();
//                    readMessages();
//                }
////                catch (IOException e) {
//                    e.printStackTrace();
//                }
////                finally {
//                    closeConnection();
//                }
//            }).start();
        } catch (IOException e) {
            MyServer.LOGGER.error("Проблемы при создании обработчика клиента");
            throw new RuntimeException("Проблемы при создании обработчика клиента");
        }
    }

    public void authentication() throws IOException {
        while (true) {
            String str = in.readUTF();
            if (str.startsWith("/auth")) {
                String[] parts = str.split("\\s");
                String nick = myServer.getAuthService().getNickByLoginPass(parts[1], parts[2]);
                if (nick != null) {
                    if (!myServer.isNickBusy(nick)) {
                        sendMsg("/authok " + nick);
                        name = nick;
                        myServer.broadcastMsg("*" + name + " зашел в чат");
                        myServer.subscribe(this);
                        return;
                    } else {
                        sendMsg("*Учетная запись уже используется");
                    }
                } else {
                    sendMsg("*Неверные логин/пароль");
                }
            }
        }
    }

    public void readMessages() throws IOException {
        while (true) {
            String str = in.readUTF();
            if (str.startsWith("/")) {
                if (str.equals("/end")) {
                    break;
                }
                if (str.startsWith("/w ")) {
                    String[] tokens = str.split("\\s");
                    String nick = tokens[1];
                    String msg = str.substring(4 + nick.length());
                    myServer.sendMsgToClient(this, nick, msg);
                }
                if (str.startsWith("/cn ")) {
                    String[] tokens = str.split("\\s");
                    String oldNick = this.getName();
                    String newNick = tokens[1];
                    System.out.println(oldNick);
                    System.out.println(newNick);
                    try {
                        myServer.getAuthService().changeNick(oldNick,newNick);
                    }
                    catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                continue;
            }
            myServer.broadcastMsg(name + ": " + str);
            MyServer.LOGGER.info(name + " прислал сообщение: " + str);
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            MyServer.LOGGER.error(e);
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        myServer.unsubscribe(this);
        myServer.broadcastMsg("*" + name + " вышел из чата");
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

