import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class DatagramReceiver implements Runnable {

    private final int port;

    public DatagramReceiver(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("Receiver listening on port: " + port);

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Message received: " + message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
