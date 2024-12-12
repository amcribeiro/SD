import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private boolean isLoggedIn = false;
    private String currentUser = null; // Nome do utilizador logado

    public static void main(String[] args) {
        new Client().start();
    }

    public void start() {
        try {
            socket = new Socket(SERVER_ADDRESS, PORT);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Conectado ao servidor.");

            showInitialMenu();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showInitialMenu() {
        Scanner scanner = new Scanner(System.in);

        while (!isLoggedIn) {
            System.out.println("\n--- MENU INICIAL ---");
            System.out.println("1. Registar");
            System.out.println("2. Login");
            System.out.println("3. Sair");
            System.out.print("Escolha uma opção: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    handleRegister(scanner);
                    break;
                case 2:
                    handleLogin(scanner);
                    break;
                case 3:
                    System.out.println("A sair...");
                    return;
                default:
                    System.out.println("Opção inválida!");
            }
        }

        // Se o utilizador fez login com sucesso, mostrar o menu principal
        showMainMenu(scanner);
    }

    private void showMainMenu(Scanner scanner) {
        boolean running = true;

        while (running) {
            System.out.println("\n--- MENU PRINCIPAL ---");
            System.out.println("1. Enviar Mensagem");
            System.out.println("2. Enviar Alerta");
            System.out.println("3. Logout");
            System.out.print("Escolha uma opção: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Limpa o buffer

            switch (choice) {
                case 1:
                    handleSendMessage(scanner);
                    break;
                case 2:
                    handleSendAlert(scanner);
                    break;
                case 3:
                    System.out.println("A fazer logout...");
                    isLoggedIn = false;
                    currentUser = null;
                    showInitialMenu(); // Retorna ao menu inicial
                    running = false;
                    break;
                default:
                    System.out.println("Opção inválida!");
            }
        }
    }

    private void handleRegister(Scanner scanner) {
        System.out.print("Introduza o username: ");
        String username = scanner.nextLine();
        System.out.print("Introduza a password: ");
        String password = scanner.nextLine();
        System.out.print("Introduza o nível (user/admin): ");
        String level = scanner.nextLine();

        writer.println("REGISTER:" + username + ":" + password + ":" + level);
        try {
            String response = reader.readLine();
            System.out.println("Servidor: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleLogin(Scanner scanner) {
        System.out.print("Introduza o username: ");
        String username = scanner.nextLine();
        System.out.print("Introduza a password: ");
        String password = scanner.nextLine();

        writer.println("LOGIN:" + username + ":" + password);
        try {
            String response = reader.readLine();
            if (response.startsWith("SUCCESS")) {
                System.out.println("Login bem-sucedido!");
                isLoggedIn = true;
                currentUser = username;

                new Thread(this::listenForMessages).start();
            } else {
                System.out.println("Erro: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenForMessages() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("\n[Servidor]: " + message);

            }
        } catch (IOException e) {
            System.out.println("Conexão com o servidor perdida.");
        }
    }


    private void handleSendMessage(Scanner scanner) {
        System.out.print("Introduza o destinatário: ");
        String recipient = scanner.nextLine();
        System.out.print("Introduza a mensagem: ");
        String message = scanner.nextLine();

        writer.println("SEND:" + recipient + ":" + message);
        try {
            if (reader.ready()) {
                String response = reader.readLine();
                System.out.println("Servidor: " + response);
            } else {
                System.out.println("Mensagem enviada com sucesso.");
            }
        } catch (IOException e) {
            System.out.println("Erro ao enviar a mensagem: " + e.getMessage());
        }
    }


    private void handleSendAlert(Scanner scanner) {
        System.out.print("Introduza o alerta: ");
        String alert = scanner.nextLine();

        writer.println("ALERT:" + alert);
        try {
            String response = reader.readLine();
            System.out.println("Servidor: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
