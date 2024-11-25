import java.util.Scanner;
import java.io.*;
import java.net.*;

public class MainMenu {


    public void Menu(LocalDatabase localDatabase, PrintWriter out, BufferedReader in, Scanner scanner) {
        int choice = 0;
        do {
            displayMainMenu(out);
            try {
                choice = Integer.parseInt(in.readLine());
            } catch (NumberFormatException | IOException e) {
                out.println("Choose a valid option.");
                continue;
            }

            switch (choice) {
                case 1:
                    RegisterMenu registerMenu = new RegisterMenu();
                    User newUser = new User(null, null, null, 0);

                    try {
                        newUser = registerMenu.getRegisterData(out, in);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (newUser != null) {
                        localDatabase.addNewUser(newUser);
                    }

                    out.println("User registered successfully.");
                    break;

                case 2:
                    LoginMenu loginMenu = new LoginMenu();
                    Login login = new Login(null, null);

                    try {
                        login = loginMenu.getLoginInfo(out, in);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    User verifyLogin = localDatabase.verifyLoginData(login);

                    if (verifyLogin != null) {
                        out.println("Login successful.");
                        out.println("Logged in as " + verifyLogin.getName() + " | Level: " + verifyLogin.getLevel());

                        //TODO: Implementar menu de mensagens
                        //MenuMessages menuMessages = new MenuMessages(out, in);
                        //menuMessages.sendMessageMenu(logincheck);
                    } else {
                        out.println("Login failed.");
                    }

                case 3:
                    out.println("Exiting...");
                    System.exit(0);
                    break;

                default:
                    out.println("Enter valid option.");
                    break;
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
