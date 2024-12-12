import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class Channel {
    private String name;                  // Nome do canal
    private Set<User> members;           // Membros do canal

    public Channel(String name) {
        this.name = name;
        this.members = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public Set<User> getMembers() {
        return members;
    }

    // Adicionar membro ao canal
    public synchronized boolean addMember(User user) {
        return members.add(user); // Retorna false se o membro já estiver no canal
    }

    // Remover membro do canal
    public synchronized boolean removeMember(User user) {
        return members.remove(user);
    }

    // Enviar mensagem para todos os membros do canal
    public synchronized void broadcastMessage(String sender, String message) {
        for (User member : members) {
            if (member.getSocket() != null) { // Verificar se o membro está conectado
                try {
                    PrintWriter writer = new PrintWriter(member.getSocket().getOutputStream(), true);
                    writer.println("CHANNEL_MSG:" + name + ":" + sender + ":" + message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Channel{name='" + name + "', members=" + members.size() + '}';
    }
}
