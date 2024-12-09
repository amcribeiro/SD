import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BroadcastSender implements Runnable{
    MulticastSocket socket;
    User user;
    String message;

    public BroadcastSender(User user, String message) {
        this.socket = null;
        this.user = user;
        this.message = message;
    }

    @Override
    public void run( ) {
        try {
            socket = new MulticastSocket(ServerConfig.PORT_BROADCAST);
            InetAddress address = InetAddress.getByName(ServerConfig.IP_BROADCAST);
            socket.joinGroup(address);

            while (true) {
                Thread.sleep(600000);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                String date = formatter.format(LocalDateTime.now());
                byte[] msg = ("\nAlerta do " + user.getName() + " | " +
                        user.getLevel() + " : " + message).getBytes();
                DatagramPacket packet = new DatagramPacket(msg, msg.length, address, ServerConfig.PORT_BROADCAST);
                socket.send(packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
