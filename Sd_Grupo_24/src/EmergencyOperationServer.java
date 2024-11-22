import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class EmergencyOperationServer {

    private static class EmergencyOperationServerUserCountState {
        //AtomicInteger usado em vez de int para garantir que é thread safe
        AtomicInteger currentUserCount = new AtomicInteger(0);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java EmergencyOperationServer <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);

        LocalDatabase database = new LocalDatabase();
        EmergencyOperationServerUserCountState serverState = new EmergencyOperationServerUserCountState();

        try (ServerSocket serverSocket = new ServerSocket(portNumber);
             Scanner scanner = new Scanner(System.in)) {
            System.out.println("Started the server...");

            ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

            executorService.scheduleAtFixedRate(() -> {
                System.out.println("Active users: " + serverState.currentUserCount.get());
            }, 0, 400, TimeUnit.SECONDS);


            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();

                    serverState.currentUserCount.incrementAndGet();
                    Thread clientThread = new Thread(() -> {
                        try (
                                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        ) {
                            MainMenu mainMenu = new MainMenu();
                            mainMenu.Menu(database, out, in, scanner);

                        } catch (IOException e) {
                            System.out.println("Error connecting to client: " + e.getMessage());
                        } finally {
                            serverState.currentUserCount.decrementAndGet();
                        }
                    });

                    clientThread.start();

                } catch (IOException e) {
                    System.out.println("Error accepting client connection: " + e.getMessage());
                    break;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}