import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EmergencyProtocol {

    private PrintWriter out;
    private BufferedReader in;

    private static final int WAITING = 0;
    private static final int SENTCOMMAND = 1;
    private static final int ANOTHER = 2;

    private static final int NUMCOMMANDS = 5;

    private int currentState = WAITING;
    private int command = 0;

    private static final String[] VALID_COMMANDS = {
            "PRIVATE_MESSAGE",
            "CHAT",
            "LIST_MESSAGES",
            "REQUEST",
            "APPROVE",
            "BROADCAST",
            "MULTICAST",
            "HELP",
    };

    private User currentUser;

    public EmergencyProtocol(PrintWriter out, BufferedReader in) {
        this.out = out;
        this.in = in;
    }

    public void setUser(User user) {
        this.currentUser = user;
    }

    public String processInput(String input) throws IOException {
        String output = null;

        if (currentState == WAITING) {
            output = "----- Welcome " + currentUser.getName() + " | " + currentUser.getLevel() + " -----\n"
                    + "Enter a command!" + "\n type command HELP to get list of commands";
            currentState = SENTCOMMAND;
        } else if (currentState == SENTCOMMAND) {
            if (input.equalsIgnoreCase("HELP")) {
                output = "Commands: \n"
                        + "PRIVATE_MESSAGE - Send a private message to a user.\n"
                        + "CHAT - Start a chat with a group.\n"
                        + "LIST_MESSAGES - List all messages.\n"
                        + "REQUEST - Request a multicast message.\n"
                        + "APPROVE - Approve a request.\n"
                        + "BROADCAST - Broadcast a message.\n"
                        + "MULTICAST - Send a multicast message.\n"
                        + "HELP - Show this help message.";
            } else if (isCommandValid(input)) {
                output = "Executing command: " + input + ". Want to type another command? (y/n)";
                currentState = ANOTHER;
            } else if (input.equalsIgnoreCase("Logout")) {
                return "Logout";
            } else {
                output = "Invalid command! Enter a valid command from the commands list!";
            }
        } else if (currentState == ANOTHER) {
            if (input.equalsIgnoreCase("y")) {
                output = "Enter another command.";
                command = (command + 1) % NUMCOMMANDS;
                currentState = SENTCOMMAND;
            } else {
                output = "Bye, " + currentUser.getName() + ".";
                currentState = WAITING;
            }
        }

        return output;

    }

    private boolean isCommandValid(String input) throws IOException {
        String[] inputParts = input.split(" ");
        String command = inputParts[0].toUpperCase();

        for (String validCommand : VALID_COMMANDS) {
            if (command.equalsIgnoreCase(validCommand)) {
                switch (command) {
                    case "PRIVATE_MESSAGE":
                        if (inputParts.length < 3 || !input.contains("-")) {
                            out.println("Private Message Format: PRIVATE_MESSAGE <message> - <recipient IP>:<port>");
                            return false;
                        }

                        String[] parts = input.split(" - ");
                        String message = parts[0].substring("PRIVATE_MESSAGE ".length()).trim();
                        String[] recipientDetails = parts[1].trim().split(":");

                        if (recipientDetails.length != 2) {
                            out.println("Invalid recipient format. Use <IP>:<port>");
                            return false;
                        }

                        String recipientIP = recipientDetails[0];
                        int recipientPort;
                        try {
                            recipientPort = Integer.parseInt(recipientDetails[1]);
                        } catch (NumberFormatException e) {
                            out.println("Invalid port number.");
                            return false;
                        }

                        new Thread(() -> {
                            DatagramSender sender = new DatagramSender();
                            sender.sendMessage(message, recipientIP, recipientPort);
                        }).start();

                        out.println("Private message sent.");
                        return true;


                    case "CHAT":
                        String groupAddress = chooseGroupAccordingToLevel();
                        out.println("Chat started with group: " + groupAddress);
                        return true;

                    case "LIST_MESSAGES":
                        getAllMessages();
                        return true;

                    case "REQUEST":
                        out.println("Request Format Is: REQUEST <message>");
                        return false;

                    case "APPROVE":
                        if (currentUser.getLevel() >= 1 && currentUser.getLevel() <= 3) {
                            if (showAndApproveRequestFlow()) {
                                out.println("Enter the request number to approve:");
                                int requestNumber = Integer.parseInt(in.readLine());
                                boolean approved = showAndApproveRequest(requestNumber);
                                if (!approved) {
                                    out.println("Approval failed. Either the request number is invalid or no matching request exists.");
                                }
                            }
                            return true;
                        }
                        return false;

                    case "BROADCAST":
                        if (currentUser.getLevel() >= 1 && currentUser.getLevel() <= 3) {
                            if (inputParts.length < 2) {
                                out.println("Broadcast Format is: BROADCAST <message>");
                                return false;
                            }
                            String messageToBroadcast = String.join(" ", Arrays.copyOfRange(inputParts, 1, inputParts.length));
                            new Thread(new BroadcastSender(currentUser, messageToBroadcast)).start();
                            out.println("Broadcast message sent successfully.");
                            return true;
                        }
                        return false;

                    case "MULTICAST":
                        if (currentUser.getLevel() >= 1 && currentUser.getLevel() <= 2) {
                            multicastMessage();
                            return true;
                        }
                        return false;
                    case "HELP":
                        out.println("Commands: ");
                        out.println("PRIVATE_MESSAGE - Send a private message to a user.");
                        out.println("CHAT - Start a chat with a group.");
                        out.println("LIST_MESSAGES - List all messages.");
                        out.println("REQUEST - Request a multicast message.");
                        out.println("APPROVE - Approve a request.");
                        out.println("BROADCAST - Broadcast a message.");
                        out.println("MULTICAST - Send a multicast message.");
                        out.println("HELP - Show this help message.");
                        return false;
                    default:
                        break;
                }
            }
        }
        return false;
    }

    private void multicastMessage() throws IOException {
        int groupChoice = chooseMulticastGroupByLevelOfUser();
        String groupMulticast = ServerConfig.IP_LOW_LEVEL;
        int portMulticast = ServerConfig.PORT_LOW_LEVEL;
        String type = "Low Level";

        if (groupChoice == 2) {
            groupMulticast = ServerConfig.IP_MEDIUM_LEVEL;
            portMulticast = ServerConfig.PORT_MEDIUM_LEVEL;
            type = "Medium Level";
        } else if (groupChoice == 3) {
            groupMulticast = ServerConfig.IP_HIGH_LEVEL;
            portMulticast = ServerConfig.PORT_HIGH_LEVEL;
            type = "High Level";
        }

        out.println("Type the request to be sent:");
        String instruction = in.readLine();
        new Thread(new MulticastSender(currentUser, instruction, groupMulticast, portMulticast)).start();
        requestMultiCastRequest("MULTICAST_" + type, instruction);
    }

    private void requestMultiCastRequest(String type, String instruction) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("requests.csv", true))) {
            writer.println(currentUser.getName() + " | " + currentUser.getLevel() + "," + instruction + ", " + type);
            out.println("Instruction Saved.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean showAndApproveRequestFlow() {
        boolean hasPendingApprovals = false;

        try (BufferedReader reader = new BufferedReader(new FileReader("requests.csv"))) {
            String line;
            int count = 1;

            out.println("----- Requests Pending Approval -----");

            while ((line = reader.readLine()) != null) {
                String[] items = line.split(",");
                if (items.length == 3 && items[2].trim().equalsIgnoreCase("PENDING")
                        && Integer.parseInt(items[0]) <= currentUser.getLevel()) {

                    out.println(count + ". " + line);
                    count++;
                    hasPendingApprovals = true;
                }
            }

            if (!hasPendingApprovals) {
                out.println("\n----- No Requests Left To Approve -----\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return hasPendingApprovals;
    }

    private boolean showAndApproveRequest(int requestNumber) {
        List<String> lines = new ArrayList<>();
        boolean approved = false;
        boolean hasPendingApprovals = false;

        try (BufferedReader reader = new BufferedReader(new FileReader("requests.csv"))) {
            String line;
            int count = 1;

            out.println("----- Requests Pending Approval -----");

            while ((line = reader.readLine()) != null) {
                String[] items = line.split(",");
                if (items.length == 3 && items[2].trim().equalsIgnoreCase("PENDING")
                        && Integer.parseInt(items[0]) <= currentUser.getLevel()) {

                    out.println(count + ". " + line);
                    if (count == requestNumber) {
                        items[2] = "APPROVED";
                        approved = true;
                    }
                    count++;
                    hasPendingApprovals = true;
                }
                lines.add(String.join(",", items));
            }

            if (!hasPendingApprovals) {
                out.println("\n----- No Requests Left To Approve -----\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter("requests.csv"))) {
            for (String line : lines) {
                writer.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (approved) {
            out.println("Approved request number " + requestNumber + ".");
        } else {
            out.println("Invalid request number or no matching pending request.");
        }

        return approved;
    }


    private void getAllMessages() {
        try (BufferedReader reader = new BufferedReader(new FileReader("messages.csv"))) {
            String line;
            boolean hasUnreadMessages = false;

            out.println("----- Unread Messages -----");

            while ((line = reader.readLine()) != null) {
                if (isMessageReceiverCurrentUser(line)) {
                    out.println(line);
                    hasUnreadMessages = true;
                }
            }

            if (!hasUnreadMessages) {
                out.println("\n ----- No unread messages for the current user. ----- \n");
            }
        } catch (IOException e) {
            System.err.println("Error checking unread messages: " + e.getMessage());
        }
    }

    private boolean isMessageReceiverCurrentUser(String line) {
        String[] items = line.split(" - ");
        return items.length == 2 && items[1].trim().startsWith(currentUser.getName());
    }

    private String chooseGroupAccordingToLevel() throws IOException {
        int option;

        do {
            showGroupAccordingToLevel();
            option = Integer.parseInt(in.readLine());
        } while (!isGroupOptionValidForCurrentUserLevel(option));

        return chosenGroupAddress(option);
    }

    public int chooseMulticastGroupByLevelOfUser() throws IOException {
        int chosenGroup;

        do {
            showAvailableGroups();
            chosenGroup = Integer.parseInt(in.readLine());
        } while (!isValidGroupChoiceForUserLevel(chosenGroup));

        return chosenGroup;
    }

    public void showAvailableGroups() {
        out.println("----- Groups -----");
        out.println("1. Low Level Chat");

        if (currentUser.getLevel() == 2) {
            out.println("2. Medium Level Chat");
        } else if (currentUser.getLevel() == 3) {
            out.println("2. Medium Level Chat");
            out.println("3. High Level Chat");
        }
    }

    public boolean isValidGroupChoiceForUserLevel(int option) {
        if (currentUser.getLevel() == 1) {
            return option == 1;
        } else if (currentUser.getLevel() == 2) {
            return option >= 1 && option <= 2;
        } else if (currentUser.getLevel() == 3) {
            return option >= 1 && option <= 3;
        }
        return false;
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
