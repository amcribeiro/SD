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

    // Método para criar canais
    public synchronized boolean createChannel(String channelName) {
        if (channels.containsKey(channelName)) {
            return false; // Canal já existe
        }
        channels.put(channelName, new Channel(channelName));
        return true;
    }

    // Método para obter um canal
    public synchronized Channel getChannel(String channelName) {
        return channels.get(channelName);
    }

    public Map<String, ClientHandler> getOnlineUsers() {
        return onlineUsers;
    }
}

