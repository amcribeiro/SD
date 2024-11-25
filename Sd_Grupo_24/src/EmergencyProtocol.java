import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class EmergencyProtocol {

    PrintWriter out;
    BufferedReader in;

    private static final int WAITING = 0; //Atribuimos valor inteiro para ser mais faicl
    private static final int SENTCOMMAND = 1;

    private int currentState = WAITING;

    public EmergencyProtocol(PrintWriter out, BufferedReader in) {
        this.out = out;
        this.in = in;
    }

    private String[] validCommandList = {
            "PRIVATE_MESSAGE",
            "CHAT",
            "LIST_MESSAGES",
            "REQUEST",
            "APPROVE",
            "BROADCAST",
            "MULTICAST"
    };

    private User currentUser;

    public void setUser(User user) {
        currentUser = user;
    }

    public String processInput(String input) {
        String output = null;

        if (currentState == WAITING){
            output = "----- Welcome " + currentUser.getName() + " | "+ currentUser.getLevel()+ " -----\n" + "Enter a command!" ;
            currentState = SENTCOMMAND;
        } else if (currentState == SENTCOMMAND){
            if(isCommandValid(input)){

            }
        }

        return output;
    }

    private boolean isCommandValid(String command) {

        for(String validCommand : validCommandList){
            if (command.equalsIgnoreCase(validCommand)) {
                switch (command) {
                    case "PRIVATE_MESSAGE":
                        out.println("Private Message Format: PRIVATE <message> - <recipient>");
                        return false;
                    case "CHAT":
                        String groupAddress;
                        try {
                            groupAddress = chooseGroupAccordingToLevel();
                            out.println(groupAddress);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return true;
                    case "LIST_MESSAGES":
                        getAllMessages();
                        return true;
                    case "REQUEST":
                        out.println("Deves escrever uma instrucao depois de REQUEST!");
                        out.println("EX: REQUEST Atacar Guantanamo");
                        return false;
                    case "APPROVE":
                        if ("Admiral".equals(user.getmilitaryRank()) || "Lieutenant".equals(user.getmilitaryRank())) {
                            if(listPendingApprovals()){
                                int requestNum = Integer.parseInt(in.readLine());
                                approveInstruction(requestNum);
                            }
                            return true;
                        }
                        return false;
                    case "BROADCAST":
                        if ("Admiral".equals(user.getmilitaryRank()) || "Lieutenant".equals(user.getmilitaryRank())) {
                            out.println("Deves escrever uma instrucao depois de BROADCAST!");
                            out.println("EX: BROADCAST Atacar Guantanamo!");
                            return true;
                        }
                        return false;
                    case "MULTICAST":
                        if ("Admiral".equals(user.getmilitaryRank()) || "Lieutenant".equals(user.getmilitaryRank())) {
                            SendMulticastMessage();
                            return true;
                        }
                        return false;
                    default:
                        break;
                }

            }
        }
    }

    private void getAllMessages() {
        boolean hasPendingMessages = false;

        try (BufferedReader reader = new BufferedReader(new FileReader("messages.csv"))) {
            String line;

            out.println(" === Mensanges guardadas ===");

            while ((line = reader.readLine()) != null) {
                if (isMessageForUser(line)) {
                    out.println(line);
                    hasPendingInstructions = true;
                }
            }
            if (!hasPendingInstructions) {
                out.println("\n -- Não existem mensagens para ti! -- \n");
            }
        } catch (IOException e) {
            System.err.println("Erro ao verificar mensagens: " + e.getMessage());
        }
    }

    private String chooseGroupAccordingToLevel() throws IOException {
        int option;

        do {
            showGroupAccordingToLevel();
            option = Integer.parseInt(in.readLine());
        } while (!isGroupOptionValidForCurrentUserLevel(option));

        return chosenGroupAddress(option);
    }

    private String chosenGroupAddress(int option) {
        String startChat = "STARTCHAT ";
        if (option == 1) {
            startChat += ServerConfig.IP_LOW_LEVEL + " " + ServerConfig.PORT_LOW_LEVEL;
        } else if (option == 2){
            startChat += ServerConfig.IP_MEDIUM_LEVEL + " " + ServerConfig.PORT_MEDIUM_LEVEL;
        } else if (option == 3){
            startChat += ServerConfig.IP_HIGH_LEVEL + " " + ServerConfig.PORT_HIGH_LEVEL;
        }

        return startChat;
    }

    private boolean isGroupOptionValidForCurrentUserLevel(int option) {
        if(this.currentUser.getLevel() == 1){
            return option == 1;
        } else if (this.currentUser.getLevel() == 2){
            return option >= 1 && option <= 2;
        }  else if (this.currentUser.getLevel() == 3){
            return option >= 1 && option <= 3;
        }

        return false;
    }

    private void showGroupAccordingToLevel() {
        out.println("---- GROUPS -----");

        out.println("1 - Low Level Chat"); //Todos os niveis 1 a 3 tem acessoa  este chat

        if(this.currentUser.getLevel() == 2){
            out.println("2 - Medium Level Chat");
        }else if (this.currentUser.getLevel() == 3){
            out.println("2 - Medium Level Chat");
            out.println("3 - High Level Chat");
        }
    }
}
