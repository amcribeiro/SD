import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

    private boolean isCommandValid(String command) throws IOException {

        for(String validCommand : validCommandList){
            if (command.equalsIgnoreCase(validCommand)) {
                switch (command) {
                    case "PRIVATE_MESSAGE":
                        out.println("Private Message Format Is: PRIVATE <message> - <recipient>");
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
                        out.println("Request Format Is: REQUEST <message>");
                        return false;
                    case "APPROVE":
                        if (currentUser.getLevel() >= 1 && currentUser.getLevel() <= 3) {
                            if(showPendingApprovalRequests()){
                                int requestNumber = Integer.parseInt(in.readLine());
                                approveRequest(requestNumber);
                            }

                            return true;
                        }
                        return false;
                    case "BROADCAST":
                        if (currentUser.getLevel() >= 1 || currentUser.getLevel() <= 3) {
                            out.println("Broadcast Format is: BROADCAST <message>");
                            return true;
                        }
                        return false;
                    case "MULTICAST":
                        if (currentUser.getLevel() >= 1 || currentUser.getLevel() <= 2) {
                            SendMessageUsingMulticast();
                            return true;
                        }
                        return false;
                    default:
                        break;
                }

            }
        }
    }

    private void SendMessageUsingMulticast() {
        String group_multicast = "226.0.0.0";
        int port_multicast = 4000;
        try {
            int group_address = chooseMulticastGroupByRank();
            String type = "";
            if(group_address == 1){
                group_multicast = ServerConfig.IP_LOW_LEVEL;
                port_multicast = ServerConfig.PORT_LOW_LEVEL;
                type = "Low Level";
            }else if(group_address == 2){
                group_multicast = ServerConfig.IP_MEDIUM_LEVEL;
                port_multicast = ServerConfig.PORT_MEDIUM_LEVEL;
                type = "Medium Level";
            }else if(group_address == 3){
                group_multicast = ServerConfig.IP_HIGH_LEVEL;
                port_multicast = ServerConfig.PORT_HIGH_LEVEL;
                type = "High Level";
            }

            out.println("Type the request to be sent: ");

            String instruction = in.readLine();
            new Thread(new MulticastSender(user, instruction, group_multicast, port_multicast)).start();
            requestMultiCastInstruction("MULTICAST_"+type, instruction);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void approveRequest(int requestNumber) {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader("requests.csv"))) {
            String line;
            int count = 1;

            while ((line = reader.readLine()) != null) {
                String[] items = line.split(",");

                if (items.length == 3 && items[2].equalsIgnoreCase("PENDING")) {
                    if (count == requestNumber) {
                        items[2] = "APPROVED";
                    }
                    count++;
                }

                lines.add(String.join(",", items));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter("requests.csv"))) {
            for (String line : lines) {
                writer.println(line);
            }
            out.println("Approved the request.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean showPendingApprovalRequests() {
        boolean hasPendingApprovals = false;

        try (BufferedReader reader = new BufferedReader(new FileReader("requests.csv"))) {
            String line;
            int count = 1;

            out.println("----- Requests Pending Approval -----");

            while ((line = reader.readLine()) != null) {
                String[] items = line.split(",");

                if (items.length == 3 && items[2].equalsIgnoreCase("PENDING")) {
                    out.println(count + ". " + line);
                    count++;
                    hasPendingApprovals = true;
                }
            }

            if (!hasPendingApprovals) {
                out.println("\n ----- No Requests Left To Approve ----- \n");
                return false;
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void getAllMessages() {
        boolean hasUnreadMessages = false;

        try (BufferedReader reader = new BufferedReader(new FileReader("messages.csv"))) {
            String line;

            out.println("----- Unread Messages -----");

            while ((line = reader.readLine()) != null) {
                if (isMessageReceiverCurrentUser(line)) {
                    out.println(line);
                    hasUnreadMessages = true;
                }
            }
            if (!hasUnreadMessages) {
                out.println("\n ---- Current user doesnt have any unread messages! ----- \n");
            }
        } catch (IOException e) {
            System.err.println("Error checking unread messages: " + e.getMessage());
        }
    }

    private boolean isMessageReceiverCurrentUser(String line) {
        String[] items = line.split(" - ");

        if (items.length == 2) {
            String sentTo = items[1].trim();

            if (sentTo.startsWith(": ")) {
                sentTo = sentTo.substring(2);
            }

            String receiver = sentTo.split(" : ")[0];

            return receiver.equals(currentUser.getName()); //Veriffica se o user autal e qwuem recebe a mensagem
        }

        return false;
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
