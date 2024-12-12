import java.io.*;
import java.net.*;
import java.util.*;

class ClientHandler extends Thread {
    private Socket socket;
    private Server server;
    private User currentUser;
    private BufferedReader reader;
    private PrintWriter writer;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            // Process messages from the client
            String message;
            while ((message = reader.readLine()) != null) {
                processMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (currentUser != null) {
                server.unregisterUser(currentUser.getUsername());
                System.out.println("Usuário " + currentUser.getUsername() + " desconectado.");
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processMessage(String message) {
        String[] parts = message.split(":");
        String command = parts[0];

        switch (command) {
            case "REGISTER":
                handleRegister(parts);
                break;
            case "LOGIN":
                handleLogin(parts);
                break;
            case "SEND":
                handleSendMessage(parts);
                break;
            default:
                writer.println("ERROR:Invalid command");
        }
    }

    private void handleRegister(String[] parts) {
        if (parts.length != 4) {
            writer.println("ERROR:Invalid format");
            return;
        }
        String username = parts[1];
        String password = parts[2];
        String level = parts[3];

        if (UserManager.userExists(username)) {
            writer.println("ERROR:User already exists");
            return;
        }

        int newId = UserManager.getNextId();
        User newUser = new User(newId, username, password, level);
        UserManager.saveUser(newUser);
        writer.println("SUCCESS:Registration successful");
    }

    private void handleLogin(String[] parts) {
        if (parts.length != 3) {
            writer.println("ERROR:Invalid format");
            return;
        }
        String username = parts[1];
        String password = parts[2];

        User user = UserManager.authenticate(username, password);
        if (user != null) {
            currentUser = user;
            server.registerUser(username, this); // Register user as online
            writer.println("SUCCESS:Login successful");
        } else {
            writer.println("ERROR:Invalid credentials");
        }
    }

    private void handleSendMessage(String[] parts) {
        if (parts.length < 3) {
            writer.println("ERROR:Invalid format");
            return;
        }
        String recipient = parts[1];
        String content = String.join(":", Arrays.copyOfRange(parts, 2, parts.length));

        ClientHandler recipientHandler = server.getOnlineUser(recipient);
        if (recipientHandler != null) {
            recipientHandler.sendMessage("MESSAGE from " + currentUser.getUsername() + ": " + content);
            writer.println("SUCCESS:Message sent to " + recipient);
        } else {
            // Destinatário não está online, salvar mensagem no CSV
            saveMessageToCsv(currentUser.getUsername(), recipient, content);
            writer.println("SUCCESS:User not online, message saved for later delivery");
        }
    }

    private void saveMessageToCsv(String sender, String recipient, String message) {
        String fileName = "src/pending_messages.csv";
        try (FileWriter fw = new FileWriter(fileName, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {
            String timestamp = new Date().toString();
            pw.println(sender + "," + recipient + "," + message + "," + timestamp);
        } catch (IOException e) {
            e.printStackTrace();
            writer.println("ERROR:Could not save the message");
        }
    }


    public void sendMessage(String message) {
        writer.println(message);
    }
}
