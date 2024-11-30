import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MulticastSender implements Runnable {
    MulticastSocket socket;

    User user;
    String groupAddress;
    String messageToSend;
    int port;

    public MulticastSender(User user, String groupAddress, String messageToSend, int port) {
        this.socket = null;
        this.user = user;
        this.groupAddress = groupAddress;
        this.messageToSend = messageToSend;
        this.port = port;
    }

    @Override
    public void run(){
        try{
            EmergencyAlert call = new EmergencyAlert(port, groupAddress);

            while (true) {
                Thread.sleep(600000);
                sendMsg(call);
            }
        }catch(IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private class EmergencyAlert {
        MulticastSocket socket;
        InetAddress address;
        int port;

        public EmergencyAlert(int port, String ip) throws IOException {
            this.socket = new MulticastSocket(port);
            this.address = InetAddress.getByName(ip);
            this.port = port;
        }
    }

    private void sendMsg(EmergencyAlert emergencyAlert) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String date = formatter.format(LocalDateTime.now());
        byte[] message = ("\nComunicado do " + user.getName() + ": " + messageToSend + ", as " + date).getBytes();
        DatagramPacket packet = new DatagramPacket(message, message.length, emergencyAlert.address, emergencyAlert.port);
        emergencyAlert.socket.send(packet);
    }
}
