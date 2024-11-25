import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class EmergencyOperationClient {
    private static volatile String name;
    private static volatile int level;

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java EmergencyOperationClient <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];

        int portNumber = Integer.parseInt(args[1]);

        try (Socket kkSocket = new Socket(hostName, portNumber);

             PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);

             BufferedReader in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));

             Scanner scanner = new Scanner(System.in)) {

            // Thread de leitura
            new Thread(() -> {
                try {
                    String serverResponse;
                    while ((serverResponse = in.readLine()) != null) {
                        System.out.println("Server: " + serverResponse);

                        if (serverResponse.equalsIgnoreCase("Logged in successfully")) {
                            Thread broadcast = new Thread(new BroadcastReceiver());
                            broadcast.start();
                        }

                        if (serverResponse.toUpperCase().startsWith("BEM-VINDO")) {
                            String[] parts = serverResponse.split(" ");
                            if (parts.length >= 3) {
                                name = parts[2];
                                level = Integer.parseInt(parts[1]);
                            } else {
                                System.out.println("Formato inválido. O array 'parts' tem menos de 3 elementos.");
                            }
                        }


                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Thread de escrita
            new Thread(() -> {
                try {
                    while (true) {

                        String userInput = scanner.nextLine();
                        printClientMessage(userInput);
                        out.println(userInput);

                        if (userInput.equalsIgnoreCase("Logout")) {

                            break;
                        }
                        if (userInput.toUpperCase().startsWith("BEGINCHAT")) {
                            String[] parts = userInput.split(" ");
                            if (parts.length == 3) {
                                String address = parts[1];
                                int port = Integer.parseInt(parts[2]);
                                startMulticastChat(address, port);
                                break;
                            } else {
                                System.out.println("Formato invalido. Use: BEGINCHAT <endereco do grupo> PORT");
                            }
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            // apropriadamente)
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }
    }

    private static void printClientMessage(String message) {
        System.out.println("Client: " + message);
    }

    private static void startMulticastChat(String groupAddress, int port) {
        MulticastChat multicastChat = new MulticastChat(name, level);
        multicastChat.setGroupAddress(groupAddress, port);
        Thread multicastChatThread = new Thread(multicastChat::startChat);
        multicastChatThread.start();
    }

    public String groupAddress(int line) {
        if (line == 1) {
            return "226.4.5.6";
        } else if (line == 2) {
            return "226.4.5.7";
        } else {
            return "226.4.5.8";
        }
    }


}


