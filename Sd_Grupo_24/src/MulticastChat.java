import java.io.IOException;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class MulticastChat {
    private static final Scanner scanner = new Scanner(System.in);
    private String groupAddress;
    private int port;
    private final Semaphore receiverSemaphore = new Semaphore(1);
    private final Semaphore senderSemaphore = new Semaphore(1);
    String name;
    Integer level;

    public MulticastChat(String name, int level){
        this.name = name;
        this.level = level;
    }

    public void setGroupAddress(String groupAddress, int port) {
        this.groupAddress = groupAddress;
        this.port = port;
    }

    public void startChat() {
        System.out.println("\n====== Bem vindo ao Chat! ======\n");
        try {
            MulticastSocket socket = new MulticastSocket(port);
            InetAddress group = InetAddress.getByName(groupAddress);
            socket.joinGroup(new InetSocketAddress(group, port),
                    NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));

            Scanner scanner = new Scanner(System.in);


            Thread receiverThread = new Thread(() -> receiveMessages(socket));
            Thread senderThread = new Thread(() -> sendMessages(socket, group));

            receiverThread.start();
            senderThread.start();

            senderThread.join();
            receiverThread.interrupt();

            socket.leaveGroup(new InetSocketAddress(group, port),
                    NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
            socket.close();
            scanner.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void receiveMessages(MulticastSocket socket) {
        try {
            while (true) {
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                String received = new String(packet.getData(), 0, packet.getLength());

                receiverSemaphore.acquire();
                System.out.println("\nReceived message: " + received);
                receiverSemaphore.release();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendMessages(MulticastSocket socket, InetAddress group) {
        try {
            while (true) {

                String message = readInput(name + " | " + level + ": " + "(type 'exit' to quit): \n");
                if ("exit".equalsIgnoreCase(message)) {
                    break;
                }

                byte[] buf = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, group, port);

                senderSemaphore.acquire();
                socket.send(packet);
                senderSemaphore.release();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String readInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }


}

