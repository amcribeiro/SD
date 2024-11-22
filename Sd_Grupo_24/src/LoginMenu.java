import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class LoginMenu {
    public Login getLoginInfo(PrintWriter out, BufferedReader in) throws IOException {
        out.println("----- Login -----\"");

        out.print("Email: ");
        String email = in.readLine();

        out.print("Password: ");
        String password = in.readLine();

        return new Login(email, password);
    }
}
