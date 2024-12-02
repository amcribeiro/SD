import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class BroadcastReceiver implements Runnable {

    MulticastSocket socket;

    public BroadcastReceiver() {
        this.socket = null;
    }

    @Override
    public void run() {

        try {
            socket = new MulticastSocket(ServerConfig.PORT_BROADCAST);

            InetAddress address = InetAddress.getByName(ServerConfig.IP_BROADCAST);

            socket.joinGroup(address);
            System.out.println("Receiver started and joined multicast group.");

            while (true) {

                byte[] bytes = new byte[256]; // 256 is maximum value

                DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length);

                socket.receive(datagramPacket);

                String response = new String(datagramPacket.getData(), 0, datagramPacket.getLength());

                System.out.println(response);
            }
        } catch (Exception e) {
            System.err.println("Error occurred in BroadcastReceiver: " + e.getMessage());
            e.printStackTrace();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            e.printStackTrace();
        }
    }
}
