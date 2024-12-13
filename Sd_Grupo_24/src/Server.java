import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private Map<String, Channel> channels;
    private Map<String, ClientHandler> onlineUsers;

    public Server() {
        this.onlineUsers = new HashMap<>();
        this.channels = new HashMap<>();
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    public void start() {
        System.out.println("Servidor iniciado na porta " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nova conexão de " + clientSocket.getInetAddress());
                new ClientHandler(clientSocket, this).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void registerUser(String username, ClientHandler handler) {
        onlineUsers.put(username, handler);
    }

    public synchronized void unregisterUser(String username) {
        onlineUsers.remove(username);
    }

    public synchronized ClientHandler getOnlineUser(String username) {
        return onlineUsers.get(username);
    }

    public void broadcastAlert(String alertMessage, String senderUsername) {
        String fullMessage = "ALERT from " + senderUsername + ": " + alertMessage;

        for (ClientHandler handler : onlineUsers.values()) {
            handler.sendMessage(fullMessage);
        }

        System.out.println("Alert broadcasted: " + fullMessage);
    }

}
