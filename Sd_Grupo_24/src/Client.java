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
            System.out.println("3. Ver Mensagens Recebidas");
            System.out.println("4. Enviar Pedido de Emergência");
            System.out.println("6. Logout");
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
                    handleViewReceivedMessages();
                    break;
                case 4:
                    handleSendEmergencyRequest(scanner);
                    break;
                case 6:
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

    private void handleViewReceivedMessages() {
        writer.println("VIEW_MESSAGES");
        try {
            System.out.println("\n--- Mensagens Recebidas ---");
            String line;
            while (!(line = reader.readLine()).equals("END_OF_MESSAGES")) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Erro ao obter mensagens recebidas: " + e.getMessage());
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

    private void handleSendEmergencyRequest(Scanner scanner) {
        System.out.println("----- Menu de Operações de Emergência -----");
        System.out.println("1. Operação de Evacuação em Massa (Nível 3+)");
        System.out.println("2. Ativação de Comunicações de Emergência (Nível 2+)");
        System.out.println("3. Distribuição de Recursos de Emergência (Todos os níveis)");
        System.out.print("Escolha a operação: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        String operationType = "";
        switch (choice) {
            case 1:
                operationType = "Operacao de Evacuacao em Massa";
                break;
            case 2:
                operationType = "Ativacao de Comunicacoes de Emergencia";
                break;
            case 3:
                operationType = "Distribuicao de Recursos de Emergencia";
                break;
            default:
                System.out.println("Opção inválida!");
                return;
        }

        System.out.print("Digite a mensagem para a operação: ");
        String message = scanner.nextLine();

        writer.println("SEND_EMERGENCY_REQUEST:" + operationType + ":" + message);
        try {
            String response = reader.readLine();
            System.out.println("Servidor: " + response);
        } catch (IOException e) {
            System.out.println("Erro ao enviar o pedido de emergência: " + e.getMessage());
        }
    }

}
