import java.io.*;
import java.util.*;

public class UserManager {
    private static final String FILE_PATH = "src/users.csv";

    // Método para salvar um novo utilizador no arquivo
    public static synchronized void saveUser(User user) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(user.toString());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para carregar todos os utilizadores do arquivo
    public static synchronized List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                User user = new User(
                        Integer.parseInt(parts[0]),
                        parts[1],
                        parts[2],
                        Integer.parseInt(parts[3])
                );
                users.add(user);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    // Gerar o próximo ID com base nos IDs existentes
    public static synchronized int getNextId() {
        List<User> users = loadUsers();
        return users.isEmpty() ? 1 : users.get(users.size() - 1).getId() + 1;
    }

    // Verificar se o utilizador já existe
    public static synchronized boolean userExists(String username) {
        return loadUsers().stream().anyMatch(user -> user.getUsername().equalsIgnoreCase(username));
    }

    // Validar login
    public static synchronized User authenticate(String username, String password) {
        return loadUsers().stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username) && user.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }
}
