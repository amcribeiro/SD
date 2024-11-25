import java.io.BufferedReader;
import java.io.PrintWriter;

public class MessageMenu {
    private final PrintWriter out;
    private final BufferedReader in;

    public MessageMenu(PrintWriter out, BufferedReader in) {
        this.out = out;
        this.in = in;
    }

    public void messagesMenu(User user){
        String input;
        String output;

        try{
            EmergencyProtocol emergencyProtocol = new EmergencyProtocol(out, in);
            emergencyProtocol.setUser(user);

            output = emergencyProtocol.processInput(null);
            out.println(output);
        }
    }
}
