import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class RegisterMenu {

    public User getRegisterData(PrintWriter out, BufferedReader in) throws IOException{

        out.println("----- Register -----");

        out.println("Nome: ");
        String name = in.readLine();

        out.println("Email: ");
        String email = in.readLine();

        out.println("Password: ");
        String password = in.readLine();
        int level;

        do {

            out.println("Choose a level:");
            out.println("1 - Low");
            out.println("2 - Medium");
            out.println("3 - High");
            out.print("Enter your choice: ");

            int chooseLevel = Integer.parseInt(in.readLine());

            switch (chooseLevel) {
                case 1:
                    level = 1;
                    out.println("Level set to Low.");
                    break;
                case 2:
                    level = 2;
                    out.println("Level set to Medium.");
                    break;
                case 3:
                    level = 3;
                    out.println("Level set to High.");
                    break;
                default:
                    out.println("Invalid choice, please try again.");
                    level = -1; // Mantém o loop até que uma escolha válida seja feita.
            }
        } while (level == -1);

        return new User(name, email, password, level);

    }
}
