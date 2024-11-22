import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class LocalDatabase {
    private static final String PATH = "database.csv";

    private static final String CABECALHO = "Nome;Email;Password;Level";


    private List<User> users = new ArrayList<>();

    public LocalDatabase(){
        if(users.isEmpty()){
            getUsersFromDatabase();
        }
    }

    public List<User> getUsers() {
        return users;
    }

    public synchronized void getUsersFromDatabase() {
        /*
        Limpar os users para evitar duplicados (n deve acontecer, ja que so chamamos isto
        quando users esta vazio
         */
        users.clear();

        try (BufferedReader fileReader = new BufferedReader(new FileReader(PATH))) {
            String currentLine;
            boolean skipHeader = true;

            while ((currentLine = fileReader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false; // Ignorar a primeira linha (cabeçalho)
                    continue;
                }

                String[] userFields = currentLine.split(";");

                if (userFields.length == 4) {
                    User newUser = new User(userFields);

                    users.add(newUser);
                } else {
                    System.err.println("Invalid line on the file: " + currentLine);
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public synchronized boolean addNewUser(User user) {
        if (user == null){
            throw new IllegalArgumentException("User cannot be null");
        }

        if(isEmailAlreadyInDatabase(user.getEmail())){
            System.out.println("Email already in use");
            return false;
        }

        users.add(user);

        return true;
    }

    private synchronized void saveUsersToDatabase() {
        try (PrintWriter fileWriter = new PrintWriter(new FileWriter(PATH))) {
            fileWriter.println(CABECALHO);

            for (User user : users) {
                fileWriter.println(user.toCSV());
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private synchronized boolean isEmailAlreadyInDatabase(String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)){
                return true;
            }
        }

        return false;
    }

    public synchronized User verifyLoginData(Login loginData){
        String email = loginData.getEmail();
        String password = loginData.getPassword();

        for (User user : users) {
            if (user.getEmail().equals(email) && user.getPassword().equals(password)){
                return user;
            }
        }

        return null;
    }
}
