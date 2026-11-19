package com.mycompany.poe_part_3;

import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;

class Message {
    // Track total messages created and sent across all instances
    private static int messageCounter = 0;
    private static int totalMessagesSent = 0;
    
    // Individual message properties
    private String messageID;
    private int numMessagesSent;
    private String recipient;
    private String messageText;
    private String messageHash;
    private boolean sent;
    private boolean stored;
    private boolean disregarded;
    
    /**
     * Main constructor for creating new messages from user input
     */
    public Message(String recipient, String messageText) {
        this.messageID = generateMessageID();
        this.numMessagesSent = ++messageCounter;
        this.recipient = recipient;
        this.messageText = messageText;
        this.messageHash = createMessageHash();
        this.sent = false;
        this.stored = false;
        this.disregarded = false;
    }
    
    /**
     * Special constructor for loading test data with predefined status
     */
    public Message(String recipient, String messageText, String flag) {
        this.messageID = generateMessageID();
        this.numMessagesSent = ++messageCounter;
        this.recipient = recipient;
        this.messageText = messageText;
        this.messageHash = createMessageHash();
        
        // Set the message status based on the flag parameter
        switch (flag.toLowerCase()) {
            case "sent":
                this.sent = true;
                totalMessagesSent++;
                break;
            case "stored":
                this.stored = true;
                break;
            case "disregard":
                this.disregarded = true;
                break;
        }
    }
    
    /**
     * Creates a random 10-digit number as a unique message ID
     */
    private String generateMessageID() {
        Random rand = new Random();
        long id = 1000000000L + (long)(rand.nextDouble() * 9000000000L);
        return String.valueOf(id);
    }
    
    /**
     * Validates that the message ID is properly formatted
     */
    public boolean checkMessageID() {
        return messageID != null && messageID.length() == 10;
    }
    
    /**
     * Validates South African phone number formats
     * Returns 1 for valid numbers, -1 for invalid
     */
    public int checkRecipientCell() {
        if (recipient == null || recipient.isEmpty()) {
            return -1;
        }
        
        // Clean the number by removing spaces and dashes
        String cleanNumber = recipient.replaceAll("[\\s-]", "");
        
        // Check all valid South African phone number formats
        if (cleanNumber.startsWith("+27") && cleanNumber.length() == 12) {
            return 1; // International format: +27XXXXXXXXX
        } else if (cleanNumber.startsWith("27") && cleanNumber.length() == 11) {
            return 1; // Local international: 27XXXXXXXXX
        } else if (cleanNumber.startsWith("0") && cleanNumber.length() == 10) {
            return 1; // Local format: 0XXXXXXXXX
        }
        
        return -1; // Invalid format
    }
    
    /**
     * Creates a unique hash for the message using SHA-256 encryption
     * Falls back to simpler hash if encryption fails
     */
    public String createMessageHash() {
        try {
            // Combine message data to create unique input for hashing
            String data = messageID + messageText + recipient;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            // Convert bytes to hexadecimal string
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            // Return first 16 characters of the hash
            return hexString.toString().substring(0, 16).toUpperCase();
        } catch (Exception e) {
            // Fallback method if SHA-256 fails
            String firstTwoID = messageID.length() >= 2 ? messageID.substring(0, 2) : "00";
            String[] words = messageText.split("\\s+");
            String firstWord = words.length > 0 ? words[0] : "";
            String lastWord = words.length > 1 ? words[words.length - 1] : firstWord;
            
            return (firstTwoID + ":" + numMessagesSent + ":" + firstWord + lastWord).toUpperCase();
        }
    }
    
    /**
     * Presents options to user for what to do with the message
     * Returns status message based on user choice
     */
    public String sentMessage() {
        String[] options = {"Send Message", "Disregard Message", "Store Message to send later"};
        int choice = JOptionPane.showOptionDialog(null,
                "Choose an action for the message:",
                "Message Action",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        
        // Handle user's choice and update message status
        switch (choice) {
            case 0: // Send the message immediately
                this.sent = true;
                this.stored = false;
                this.disregarded = false;
                totalMessagesSent++;
                return "Message successfully sent.";
            case 1: // Mark as disregarded
                this.disregarded = true;
                this.sent = false;
                this.stored = false;
                return "Message disregarded.";
            case 2: // Store for sending later
                this.stored = true;
                this.sent = false;
                this.disregarded = false;
                return "Message successfully stored.";
            default:
                return "Action cancelled.";
        }
    }
    
    /**
     * Returns formatted message details for display
     * Only shows details if message was actually sent
     */
    public String printMessageIt() {
        if (sent) {
            return "MessageID: " + messageID + 
                   "\nMessage Hash: " + messageHash + 
                   "\nRecipient: " + recipient + 
                   "\nMessage: " + messageText;
        }
        return "Message not sent yet.";
    }
    
    /**
     * Returns total count of sent messages across all instances
     */
    public static int returnTotalMessageIt() {
        return totalMessagesSent;
    }
    
    /**
     * Converts message to JSON format for file storage
     * DeepSeek AI assisted with JSON implementation using org.json library
     */
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("messageID", messageID);
        json.put("numMessagesSent", numMessagesSent);
        json.put("recipient", recipient);
        json.put("messageText", messageText);
        json.put("messageHash", messageHash);
        json.put("sent", sent);
        json.put("stored", stored);
        json.put("disregarded", disregarded);
        return json;
    }
    
    // GETTER METHODS - Allow other classes to access private message data
    
    public String getMessageID() { return messageID; }
    public int getNumMessagesSent() { return numMessagesSent; }
    public String getRecipient() { return recipient; }
    public String getMessageText() { return messageText; }
    public String getMessageHash() { return messageHash; }
    public boolean isSent() { return sent; }
    public boolean isStored() { return stored; }
    public boolean isDisregarded() { return disregarded; }
}

/**
 * MAIN APPLICATION CLASS - QuickChat with Part 3 Features
 * Handles user interface, data management, and all business logic
 */
public class POE_PART_3 {
    // Main list containing all messages in the system
    private List<Message> allMessages = new ArrayList<>();
    private String registeredUsername = null;
    private String registeredPassword = null;
    
    // PART 3 REQUIREMENT: Separate arrays for different message types
    private List<Message> sentMessages = new ArrayList<>();      // Successfully sent messages
    private List<Message> disregardedMessages = new ArrayList<>(); // Rejected messages
    private List<Message> storedMessages = new ArrayList<>();    // Saved for later messages
    private List<String> messageHashes = new ArrayList<>();      // All message hashes
    private List<String> messageIDs = new ArrayList<>();         // All message IDs
       
    public static void main(String[] args) {
        POE_PART_3 app = new POE_PART_3();
        app.run();
    }
    
    public void run() {
        // Load test data and existing messages from JSON file
        loadTestData();
        populateArraysFromJSON();
        
        boolean exit = false;
        
        // Keep showing main menu until user chooses to exit
        while (!exit) {
            String choice = JOptionPane.showInputDialog(
                "Welcome to QuickChat!\n\n" +
                "Choose an option:\n" +
                "1. REGISTER\n" +
                "2. LOGIN\n" +
                "3. EXIT"
            );
            
            switch (choice) {
                case "1":
                    register();
                    break;
                case "2":
                    if (login()) {
                        showQuickChatMenu(); // Successful login
                    } else {
                        JOptionPane.showMessageDialog(null, "Login failed! Please check your credentials.");
                    }
                    break;
                case "3":
                    exit = true;
                    JOptionPane.showMessageDialog(null, "Thank you for using QuickChat!");
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Invalid choice. Please try again.");
            }
        }
    }
   
    private void loadTestData() {
        // Test Data Message 1 - Sent message about cake
        allMessages.add(new Message("+27834557896", "Did you get the cake?", "sent"));
        
        // Test Data Message 2 - Stored message about being late
        allMessages.add(new Message("+27838884567", "Where are you? You are late! I have asked you to be on time.", "stored"));
        
        // Test Data Message 3 - Disregarded greeting message
        allMessages.add(new Message("+27834484567", "Yohoooo, I am at your gate.", "disregard"));
        
        // Test Data Message 4 - Sent dinner time message
        allMessages.add(new Message("0838884567", "It is dinner time!", "sent"));
        
        // Test Data Message 5 - Stored message about leaving
        allMessages.add(new Message("+27838884567", "Ok, I am leaving without you.", "stored"));
        
        // Store all test messages in JSON file for persistence
        for (Message message : allMessages) {
            storeMessageToJSON(message);
        }
        
        // Organize messages into their respective arrays
        populateArrays();
    }
    
  
    private void populateArrays() {
        // Clear all arrays before repopulating to avoid duplicates
        sentMessages.clear();
        disregardedMessages.clear();
        storedMessages.clear();
        messageHashes.clear();
        messageIDs.clear();
        
        // Sort each message into the appropriate arrays
        for (Message message : allMessages) {
            if (message.isSent()) {
                sentMessages.add(message);
            }
            if (message.isDisregarded()) {
                disregardedMessages.add(message);
            }
            if (message.isStored()) {
                storedMessages.add(message);
            }
            messageHashes.add(message.getMessageHash());
            messageIDs.add(message.getMessageID());
        }
    }
    
    /**
     * PART 3 FEATURE: Loads messages from JSON file into arrays
     * DeepSeek AI assisted with JSON file reading implementation
     * Uses org.json library for JSON parsing
     */
    private void populateArraysFromJSON() {
        try {
            String filePath = "messages.json";
            if (Files.exists(Paths.get(filePath))) {
                // Read entire file content as string
                String content = new String(Files.readAllBytes(Paths.get(filePath)));
                // Parse JSON array from file content
                JSONArray jsonArray = new JSONArray(content);
                
                // Process each message in the JSON array
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    // Create Message object from JSON data
                    Message message = new Message(
                        json.getString("recipient"),
                        json.getString("messageText")
                    );
                    // Note: In full implementation, would set other fields from JSON
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error reading JSON file: " + e.getMessage());
        }
    }
    
    private void displaySentMessagesSendersRecipients() {
        if (sentMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No sent messages found.");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("SENT MESSAGES - SENDER & RECIPIENT\n");
        sb.append("===================================\n");
        
        // List all recipients of sent messages
        for (Message message : sentMessages) {
            sb.append("Recipient: ").append(message.getRecipient())
              .append("\n------------------------\n");
        }
        
        JOptionPane.showMessageDialog(null, sb.toString());
    }
    
    private void displayLongestSentMessage() {
        if (sentMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No sent messages found.");
            return;
        }
        
        // Find the message with the most characters
        Message longestMessage = sentMessages.get(0);
        for (Message message : sentMessages) {
            if (message.getMessageText().length() > longestMessage.getMessageText().length()) {
                longestMessage = message;
            }
        }
        
        // Display the longest message details
        String result = "LONGEST SENT MESSAGE\n" +
                       "====================\n" +
                       "Recipient: " + longestMessage.getRecipient() + "\n" +
                       "Message: " + longestMessage.getMessageText() + "\n" +
                       "Length: " + longestMessage.getMessageText().length() + " characters";
        
        JOptionPane.showMessageDialog(null, result);
    }
    
    // PART 3 FEATURE C: Search for message ID and display recipient and message
    private void searchMessageByID() {
        // Get message ID from user
        String searchID = JOptionPane.showInputDialog("Enter Message ID to search:");
        if (searchID == null || searchID.trim().isEmpty()) return;
        
        // Search through all messages for matching ID
        for (Message message : allMessages) {
            if (message.getMessageID().equals(searchID)) {
                // Found the message - display its details
                String result = "MESSAGE FOUND\n" +
                               "=============\n" +
                               "Message ID: " + message.getMessageID() + "\n" +
                               "Recipient: " + message.getRecipient() + "\n" +
                               "Message: " + message.getMessageText() + "\n" +
                               "Status: " + (message.isSent() ? "Sent" : 
                                            message.isStored() ? "Stored" : "Disregarded");
                JOptionPane.showMessageDialog(null, result);
                return; // Exit after finding the message
            }
        }
        
        // If we get here, no message was found with that ID
        JOptionPane.showMessageDialog(null, "Message ID not found: " + searchID);
    }
    
    // PART 3 FEATURE D: Search for all messages sent to a particular recipient
    private void searchMessagesByRecipient() {
        // Get recipient phone number from user
        String recipient = JOptionPane.showInputDialog("Enter recipient number to search:");
        if (recipient == null || recipient.trim().isEmpty()) return;
        
        // Find all messages for this recipient
        List<Message> foundMessages = new ArrayList<>();
        for (Message message : allMessages) {
            if (message.getRecipient().equals(recipient)) {
                foundMessages.add(message);
            }
        }
        
        // Check if any messages were found
        if (foundMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No messages found for recipient: " + recipient);
            return;
        }
        
        // Display all found messages
        StringBuilder sb = new StringBuilder();
        sb.append("MESSAGES FOR RECIPIENT: ").append(recipient).append("\n");
        sb.append("==============================\n");
        
        for (Message message : foundMessages) {
            sb.append("Message: ").append(message.getMessageText())
              .append("\nStatus: ").append(message.isSent() ? "Sent" : 
                                          message.isStored() ? "Stored" : "Disregarded")
              .append("\n------------------------\n");
        }
        
        JOptionPane.showMessageDialog(null, sb.toString());
    }
    
    // PART 3 FEATURE E: Delete message using message hash
    private void deleteMessageByHash() {
        // Get hash from user
        String hash = JOptionPane.showInputDialog("Enter Message Hash to delete:");
        if (hash == null || hash.trim().isEmpty()) return;
        
        // Search for message with matching hash
        for (int i = 0; i < allMessages.size(); i++) {
            if (allMessages.get(i).getMessageHash().equals(hash)) {
                // Remove the message from main list
                Message deletedMessage = allMessages.remove(i);
                // Update JSON file to reflect deletion
                updateJSONFile();
                // Reorganize arrays after deletion
                populateArrays();
                
                // Confirm deletion to user
                JOptionPane.showMessageDialog(null, 
                    "Message successfully deleted:\n" +
                    deletedMessage.getMessageText());
                return;
            }
        }
        
        // No message found with that hash
        JOptionPane.showMessageDialog(null, "Message hash not found: " + hash);
    }
    
    // PART 3 FEATURE F: Display report of all sent messages
    private void displaySentMessagesReport() {
        if (sentMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No sent messages found.");
            return;
        }
        
        // Build comprehensive report of all sent messages
        StringBuilder sb = new StringBuilder();
        sb.append("SENT MESSAGES REPORT\n");
        sb.append("====================\n\n");
        
        // Include all required details for each sent message
        for (Message message : sentMessages) {
            sb.append("Message Hash: ").append(message.getMessageHash()).append("\n");
            sb.append("Recipient: ").append(message.getRecipient()).append("\n");
            sb.append("Message: ").append(message.getMessageText()).append("\n");
            sb.append("Message ID: ").append(message.getMessageID()).append("\n");
            sb.append("------------------------\n\n");
        }
        
        JOptionPane.showMessageDialog(null, sb.toString());
    }
    
    
     //Updates the JSON file with current message data
     //Ensures file matches current state of all messages
     
    private void updateJSONFile() {
        try {
            JSONArray jsonArray = new JSONArray();
            // Convert all messages to JSON format
            for (Message message : allMessages) {
                jsonArray.put(message.toJSON());
            }
            
            // Write updated array back to file
            try (FileWriter file = new FileWriter("messages.json")) {
                file.write(jsonArray.toString(4)); // 4 spaces for indentation
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error updating JSON file: " + e.getMessage());
        }
    }
    
    
     //USER REGISTRATION - Creates new user account with validation
     
    private void register() {
        String username;
        // Keep asking until valid username is provided
        do {
            username = JOptionPane.showInputDialog("Enter username (must contain underscore and be 5 characters long):");
            if (username == null) return; // User cancelled
        } while (!checkUserName(username));
        
        String password;
        // Keep asking until valid password is provided
        do {
            password = JOptionPane.showInputDialog("Enter password (min 8 characters, with capital, number, special char):");
            if (password == null) return; // User cancelled
        } while (!validatePassword(password));
        
        // Save registration details
        registeredUsername = username;
        registeredPassword = password;
        JOptionPane.showMessageDialog(null, "Registration successful!");
    }
    
     //USER LOGIN - Verifies user credentials against stored registration
     
    private boolean login() {
        if (registeredUsername == null) {
            JOptionPane.showMessageDialog(null, "Please register first!");
            return false;
        }
        
        // Get login credentials from user
        String username = JOptionPane.showInputDialog("Enter username:");
        String password = JOptionPane.showInputDialog("Enter password:");
        
        // Check if credentials match stored registration
        return username != null && username.equals(registeredUsername) && 
               password != null && password.equals(registeredPassword);
    }
    
    
      //MAIN CHAT MENU - Shows options after successful login
     
    private void showQuickChatMenu() {
        boolean logout = false;
        
        while (!logout) {
            String choice = JOptionPane.showInputDialog(
                "Welcome to QuickChat!\n\n" +
                "Choose an option:\n" +
                "1. Send Messages\n" +
                "2. Show recently sent messages\n" +
                "3. Display Part 3 Features\n" +
                "4. Quit"
            );
            
            switch (choice) {
                case "1":
                    sendMessages();
                    break;
                case "2":
                    displayRecentMessages();
                    break;
                case "3":
                    showPart3Features(); // Access to all Part 3 functionality
                    break;
                case "4":
                    logout = true;
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Invalid choice. Please try again.");
            }
        }
    }
    
    
     //PART 3 FEATURES MENU - Central access point for all new functionality
     
    private void showPart3Features() {
        boolean back = false;
        
        while (!back) {
            String choice = JOptionPane.showInputDialog(
                "PART 3 FEATURES\n\n" +
                "Choose an option:\n" +
                "1. Display sender/recipient of all sent messages\n" +
                "2. Display longest sent message\n" +
                "3. Search message by ID\n" +
                "4. Search messages by recipient\n" +
                "5. Delete message by hash\n" +
                "6. Display sent messages report\n" +
                "7. Back to main menu"
            );
            
            // Route to appropriate feature based on user choice
            switch (choice) {
                case "1":
                    displaySentMessagesSendersRecipients();
                    break;
                case "2":
                    displayLongestSentMessage();
                    break;
                case "3":
                    searchMessageByID();
                    break;
                case "4":
                    searchMessagesByRecipient();
                    break;
                case "5":
                    deleteMessageByHash();
                    break;
                case "6":
                    displaySentMessagesReport();
                    break;
                case "7":
                    back = true; // Return to main menu
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Invalid choice. Please try again.");
            }
        }
    }
    
    
     //Shows recently sent messages to the user
     
    private void displayRecentMessages() {
        if (sentMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No sent messages yet.");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("RECENTLY SENT MESSAGES\n");
        sb.append("======================\n");
        
        // Display details of all sent messages
        for (Message message : sentMessages) {
            sb.append(message.printMessageIt()).append("\n\n");
        }
        
        JOptionPane.showMessageDialog(null, sb.toString());
    }
    
     //Handles sending multiple messages with full validation
     
    private void sendMessages() {
        // Ask how many messages user wants to send
        String numMessagesStr = JOptionPane.showInputDialog("How many messages do you wish to enter?");
        if (numMessagesStr == null) return;
        
        try {
            int numMessages = Integer.parseInt(numMessagesStr);
            
            // Create each requested message
            for (int i = 0; i < numMessages; i++) {
                boolean validMessage = false;
                
                // Keep trying until a valid message is created
                while (!validMessage) {
                    // Get and validate recipient phone number
                    String recipient;
                    do {
                        recipient = JOptionPane.showInputDialog("Enter recipient phone number (RSA +27 format):");
                        if (recipient == null) return;
                        if (!isValidPhoneNumber(recipient)) {
                            JOptionPane.showMessageDialog(null, 
                                "Invalid RSA phone number format.\n" +
                                "Accepted formats:\n" +
                                "+27XXXXXXXXX (12 digits total - 9 numbers after +27)\n" +
                                "0XXXXXXXXX (10 digits total - 9 numbers after 0)");
                        }
                    } while (!isValidPhoneNumber(recipient));
                    
                    // Get and validate message text length
                    String messageText;
                    do {
                        messageText = JOptionPane.showInputDialog("Enter your message (max 250 characters):");
                        if (messageText == null) return;
                        
                        if (messageText.length() > 250) {
                            int excess = messageText.length() - 250;
                            JOptionPane.showMessageDialog(null, 
                                "Message exceeds 250 characters by " + excess + ", please reduce size.");
                        }
                    } while (messageText.length() > 250);
                    
                    // Create new message object
                    Message message = new Message(recipient, messageText);
                    
                    // Validate message before processing
                    if (message.checkMessageID() && message.checkRecipientCell() == 1) {
                        String actionResult = message.sentMessage();
                        JOptionPane.showMessageDialog(null, actionResult);
                        
                        // Add to main list and update storage
                        allMessages.add(message);
                        storeMessageToJSON(message);
                        populateArrays(); // Update all arrays
                        
                        // Show details if message was sent
                        if (message.isSent()) {
                            JOptionPane.showMessageDialog(null, message.printMessageIt());
                        }
                        
                        validMessage = true; // Exit validation loop
                    } else {
                        JOptionPane.showMessageDialog(null, "Message validation failed. Please try again.");
                    }
                }
            }
            
            // Show session summary
            JOptionPane.showMessageDialog(null, 
                "Total messages sent in this session: " + Message.returnTotalMessageIt());
                
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid number entered.");
        }
    }
    
    
     //Stores a single message to the JSON file
     //DeepSeek AI assisted with JSON file operations
     
    private void storeMessageToJSON(Message message) {
        try {
            JSONArray jsonArray;
            String filePath = "messages.json";
            
            // Load existing messages if file exists
            if (Files.exists(Paths.get(filePath))) {
                String content = new String(Files.readAllBytes(Paths.get(filePath)));
                jsonArray = new JSONArray(content);
            } else {
                // Create new array if file doesn't exist
                jsonArray = new JSONArray();
            }
            
            // Add new message to array
            jsonArray.put(message.toJSON());
            
            // Write updated array back to file
            try (FileWriter file = new FileWriter(filePath)) {
                file.write(jsonArray.toString(4)); // Pretty print with indentation
            }
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error storing message: " + e.getMessage());
        }
    }
    
    // VALIDATION METHODS - Reusable checks for user input
    
    /**
     * Validates username format: exactly 5 characters with underscore
     */
    public static boolean checkUserName(String username) {
        return username != null && username.length() == 5 && username.contains("_");
    }
    
    
     //Validates password strength requirements:
     // At least 8 characters
     //Contains uppercase letter
     //Contains lowercase letter  
     //Contains digit
     //Contains special character
     
    public static boolean validatePassword(String password) {
        return password != null && 
               password.length() >= 8 &&
               password.matches(".*[A-Z].*") && // Uppercase check
               password.matches(".*[a-z].*") && // Lowercase check
               password.matches(".*\\d.*") &&   // Digit check
               password.matches(".*[^A-Za-z0-9].*"); // Special character check
    }
    
    
     //Validates South African phone number formats
     
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return false;
        // Remove spaces and dashes for validation
        String cleanNumber = phoneNumber.replaceAll("[\\s-]", "");
        
        // Check all valid formats using regular expressions
        return cleanNumber.matches("^\\+27[0-9]{9}$") || // +27 followed by 9 digits
               cleanNumber.matches("^27[0-9]{9}$") ||    // 27 followed by 9 digits
               cleanNumber.matches("^0[0-9]{9}$");       // 0 followed by 9 digits
    }
}