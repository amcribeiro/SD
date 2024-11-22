import java.util.Scanner;
import java.io.*;
import java.net.*;
public class MainMenu {


    public void Menu(PrintWriter out,BufferedReader in, Scanner scanner) {
        int choice = 0;
        do {
            displayMainMenu(out);
            try {
                choice = Integer.parseInt(in.readLine());
            } catch (NumberFormatException | IOException e) {
                out.println("Por favor, insira um número valido.");
                continue;
            }
            switch (choice){
                case 1:
                    RegisterMenu registerMenu = new RegisterMenu();
                    User newUser = new User(null, null, null, 0);
                    try {
                        newUser = registerMenu.RequestRegistrationInformation(out, in);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }










        } while (choice != 3);
        scanner.close();

    }





    private static void displayMainMenu(PrintWriter out) {
        out.println("---- Menu Emergency Operations ----");
        out.println("1. Register");
        out.println("2. Login");
        out.println("3. Exit");
        out.println("Choose an option: ");
    }
}
