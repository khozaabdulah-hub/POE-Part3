package registration;

import javax.swing.JOptionPane;

public class Registration {
    private static String firstName;
    private static String lastName;
    private static String password;
    private static String username;
    private static String phoneNumber;

    // ===== Getters & Setters =====
    public static void setFirstName(String name) { firstName = name; }
    public static String getFirstName() { return firstName; }

    public static void setLastName(String surname) { lastName = surname; }
    public static String getLastName() { return lastName; }

    public static void setUserName(String userName) { username = userName; }
    public static String getUserName() { return username; }

    public static void setPassword(String pass) { password = pass; }
    public static String getPassword() { return password; }

    public static void setPhoneNumber(String phone) { phoneNumber = phone; }
    public static String getPhoneNumber() { return phoneNumber; }

    // ===== Main Application =====
    public static void main(String[] args) {
        setFirstName(JOptionPane.showInputDialog(null, "Please enter your first name:"));
        setLastName(JOptionPane.showInputDialog(null, "Please enter your last name:"));

        // Username input
        String usernameInput;
        do {
            usernameInput = JOptionPane.showInputDialog(null, "Enter username (≤5 chars and must contain '_'):");
            if (!Login.checkUserName(usernameInput)) {
                JOptionPane.showMessageDialog(null,
                        "Username is not correctly formatted, please ensure that your username contains an underscore and is no more than five characters in length.");
            }
        } while (!Login.checkUserName(usernameInput));
        setUserName(usernameInput);

        // Password input
        String passwordInput;
        do {
            passwordInput = JOptionPane.showInputDialog(null,
                    "Enter password (≥8 chars, must include: 1 capital, 1 number, 1 special character):");
            if (!Login.checkPasswordComplexity(passwordInput)) {
                JOptionPane.showMessageDialog(null,
                        "Password is not correctly formatted, please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.");
            }
        } while (!Login.checkPasswordComplexity(passwordInput));
        setPassword(passwordInput);

        // Phone number input
        String phoneInput;
        do {
            phoneInput = JOptionPane.showInputDialog(null,
                    "Enter phone number (must start with +27 followed by 9 digits):\nExample: +27831234567");
            if (!Login.checkCellPhoneNumber(phoneInput)) {
                JOptionPane.showMessageDialog(null,
                        "Cell phone number incorrectly formatted or does not contain international code.");
            }
        } while (!Login.checkCellPhoneNumber(phoneInput));
        setPhoneNumber(phoneInput);

        // Register User
        Login login = new Login();
        String registrationResult = login.registerUser(getUserName(), getPassword(), getPhoneNumber());
        JOptionPane.showMessageDialog(null, registrationResult);

        // ===== LOGIN PROCESS =====
        boolean loginSuccessful = false;
        int attempts = 0;
        final int MAX_ATTEMPTS = 3;

        while (!loginSuccessful && attempts < MAX_ATTEMPTS) {
            String enteredUsername = JOptionPane.showInputDialog("Enter username to login:");
            String enteredPassword = JOptionPane.showInputDialog("Enter password to login:");

            login.setEnteredUsername(enteredUsername);
            login.setEnteredPassword(enteredPassword);

            if (login.loginUser()) {
                JOptionPane.showMessageDialog(null, login.returnLoginStatus());
                loginSuccessful = true;
            } else {
                attempts++;
                JOptionPane.showMessageDialog(null,
                        "Login failed. Attempt " + attempts + " of " + MAX_ATTEMPTS
                                + "\nUsername or password incorrect, please try again.");
            }
        }

        if (!loginSuccessful) {
            JOptionPane.showMessageDialog(null, "Maximum login attempts exceeded. Please try again later.");
        }
    }
}
