import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class LoginMenu {
    public Login getLoginInfo(PrintWriter out, BufferedReader in) throws IOException {
        out.println("----- Login -----\n");

        out.println("Email: ");
        String email = in.readLine();

        out.println("Password: ");
        String password = in.readLine();

        return new Login(email, password);
    }
}
