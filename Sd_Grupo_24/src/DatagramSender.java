import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class DatagramSender {

    public void sendMessage(String message, String recipientAddress, int recipientPort) {
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] buffer = message.getBytes();
            InetAddress recipient = InetAddress.getByName(recipientAddress);

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, recipient, recipientPort);
            socket.send(packet);

            System.out.println("Message sent to " + recipientAddress + ":" + recipientPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
