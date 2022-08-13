import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MyServer {
    private final int PORT = 8189;
    public static Logger LOGGER = LogManager.getLogger(MyServer.class);

    private List<ClientHandler> clients;
    private AuthService authService;


    public AuthService getAuthService() {
        return authService;
    }

    ExecutorService executorService = Executors.newFixedThreadPool(3);

    public MyServer() {
        try (ServerSocket server = new ServerSocket(PORT)) {
            authService = new BaseAuthService();
            authService.start();
            clients = new ArrayList<>();
            while (true) {
                LOGGER.info("Сервер ожидает подключения");
//                System.out.println("Сервер ожидает подключения");
                Socket socket = server.accept();
                LOGGER.info("Клиент подключился");
//                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            LOGGER.error("Ошибка в работе сервера");
//            System.out.println("Ошибка в работе сервера");

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        finally {
            if (authService != null) {
                authService.stop();
                executorService.shutdown();
            }
        }
    }

    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler o : clients) {
            if (o.getName().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void broadcastMsg(String msg) {
/*        if ((msg.length()>9)&&(msg.substring(7,9).equals("/w"))) {
            String[] parts = msg.split("\\s");
            String user = parts[2];
            System.out.println(user);
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).getName().equals(user)) {
                    clients.get(i).sendMsg(msg);
                }
            }
        }
        else {*/
        for (ClientHandler o : clients) {
            o.sendMsg(msg);}
/*        }*/
    }

    public synchronized void broadcastClientsList() {
        StringBuilder sb = new StringBuilder("/clients ");
        for (ClientHandler o : clients) {
            sb.append(o.getName() + " ");
        }
        broadcastMsg(sb.toString());
    }

    public synchronized void sendMsgToClient(ClientHandler from, String nickTo, String msg) {
        for (ClientHandler o : clients) {
            if (o.getName().equals(nickTo)) {
                o.sendMsg("от " + from.getName() + ": " + msg);
                from.sendMsg("клиенту " + nickTo + ": " + msg);
                return;
            }
        }
        from.sendMsg("*Участника с ником " + nickTo + " нет в чат-комнате");
    }

    public synchronized void unsubscribe(ClientHandler o) {
        clients.remove(o);
        broadcastClientsList();
    }

    public synchronized void subscribe(ClientHandler o) {
        clients.add(o);
        broadcastClientsList();
    }

    }

