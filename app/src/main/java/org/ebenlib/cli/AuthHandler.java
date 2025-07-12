package org.ebenlib.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
// import java.util.Scanner;

import org.ebenlib.user.User;

public class AuthHandler {

    private static final Path USER_FILE_PATH = Paths.get(
        "app", "src", "main", "resources", "users.csv"
    );
    private static final Path SESSION_FILE_PATH = Paths.get(
        "app", "src", "main", "resources", "session.csv"
    );
    // private static final Scanner fallbackScanner = new Scanner(System.in);

    public static void handle(String[] args, Map<String, String> opts) {
        if (args.length < 2) {
            printHelp();
            return;
        }
        String action = args[1];
        switch (action) {
            case "signin":
                if (guardNoActiveSession()) {
                    interactiveSignin(false);
                }
                break;
            case "signup":
                if (guardNoActiveSession()) {
                    interactiveSignup(false);
                }
                break;
            case "signout":
                handleSignout();
                break;
            default:
                ConsoleUI.error("Unknown auth action: " + action);
                printHelp();
        }
    }

    public static void printHelp() {
        ConsoleUI.header("📚 Usage: ebenlib auth [signin|signup|signout]");
        ConsoleUI.info("signin   Prompt for username/password to log in");
        ConsoleUI.info("signup   Prompt for username, password, confirm and role to register");
        ConsoleUI.info("signout  Sign out of current session");
    }

    // —— INTERACTIVE SIGNIN ——
    public static void interactiveSignin(boolean isInteractive) {
        Console console = System.console();
        String user;
        char[] pwdChars;

        try {
            // read username/password
            if (console != null) {
                user = console.readLine("Username: ");
                pwdChars = console.readPassword("Password: ");
            } else {
                user = ConsoleUI.prompt("Username:");
                pwdChars = ConsoleUI.prompt("Password:").toCharArray();
            }

            // authenticate
            String pass = new String(pwdChars);
            String role = authenticate(user, pass);
            if (role != null) {
                saveSession(user, role);
                ConsoleUI.success("Signin successful! Welcome, " + user + " (" + role + ")");
                if (isInteractive) {
                    ConsoleUI.clearScreen();;
                    new InteractiveShell(user, role)
                        .run(InteractiveMenus.getMainMenu(role, user));
                }
            } else {
                ConsoleUI.error("Signin failed: Invalid credentials.");
                // System.out.println(user + ", " + pwdChars);
            }
        } catch (Exception e) {
            ConsoleUI.error("Input error: " + e.getMessage());
        }
    }

    // —— INTERACTIVE SIGNUP ——
    public static void interactiveSignup(boolean isInteractive) {
        Console console = System.console();
        String user;
        char[] pwd1, pwd2;
        String roleInput;
        String role;

        // read fields
        if (console != null) {
            user = console.readLine("Choose a username: ");
            pwd1 = console.readPassword("Choose a password: ");
            pwd2 = console.readPassword("Confirm password: ");
            ConsoleUI.info("Choose role: 1. Librarian   2. Reader (default)");
            roleInput = console.readLine(">> ");
        } else {
            user = ConsoleUI.prompt("Choose a username:");
            pwd1 = ConsoleUI.prompt("Choose a password:").toCharArray();
            pwd2 = ConsoleUI.prompt("Confirm password:").toCharArray();
            ConsoleUI.info("Choose role: \t1. Librarian   \n\t2. Reader (default)");
            roleInput = ConsoleUI.prompt(">>");
        }

        // validate
        if (!new String(pwd1).equals(new String(pwd2))) {
            ConsoleUI.error("Passwords do not match.");
            return;
        }
        switch (roleInput.strip()) {
            case "1" -> role = "Librarian";
            case "2", "" -> role = "Reader";
            default -> {
                ConsoleUI.error("Invalid selection. Please choose 1 or 2.");
                return;
            }
        }
        if (userExists(user)) {
            ConsoleUI.error("That username is already taken.");
            return;
        }

        // persist
        try {
            Files.createDirectories(USER_FILE_PATH.getParent());
            if (!Files.exists(USER_FILE_PATH)) Files.createFile(USER_FILE_PATH);
            try (BufferedWriter writer = Files.newBufferedWriter(USER_FILE_PATH, StandardOpenOption.APPEND)) {
                writer.write(String.join(",", user, new String(pwd1), role, "true"));
                writer.newLine();
            }
            saveSession(user, role);
            ConsoleUI.success("Signup successful! You are now logged in as " + user + " (" + role + ")");
            if (isInteractive) {
                ConsoleUI.clearScreen();;
                new InteractiveShell(user, role)
                    .run(InteractiveMenus.getMainMenu(role, user));
            }
        } catch (IOException e) {
            ConsoleUI.warning("Failed to register user: " + e.getMessage());
        }
    }

    // —— SIGNOUT ——
    public static void handleSignout() {
        if (!Files.exists(SESSION_FILE_PATH)) {
            ConsoleUI.error("No active session.");
            return;
        }
        try {
            Files.delete(SESSION_FILE_PATH);
            ConsoleUI.success("You have been signed out.");
        } catch (IOException e) {
            ConsoleUI.warning("Failed to clear session: " + e.getMessage());
        }
    }

    private static boolean guardNoActiveSession() {
        if (Files.exists(SESSION_FILE_PATH)) {
            ConsoleUI.error("A user is already signed in. Please sign out first.");
            return false;
        }
        return true;
    }

    private static void saveSession(String username, String role) {
        try (BufferedWriter writer = Files.newBufferedWriter(
                SESSION_FILE_PATH,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write(String.join(",", username, role, "true"));
        } catch (IOException e) {
            ConsoleUI.warning("Failed to save session: " + e.getMessage());
        }
    }

    /** Read the current user from session file. */
    public static User getCurrentUser() {
        if (!Files.exists(SESSION_FILE_PATH)) return null;
        try (BufferedReader r = Files.newBufferedReader(SESSION_FILE_PATH)) {
            String[] parts = r.readLine().strip().split(",");
            String username = parts[0].trim();
            String role = parts[1].trim();
            boolean active  = parts.length > 2 && Boolean.parseBoolean(parts[2].trim());
            System.out.println(new User(username, role, active));
            return new User(username, role, active);
        } catch (Exception e) {
            return null;
        }
    }

    public static User requireActiveUser() {
        User u = getCurrentUser();
        if (u == null) {
            ConsoleUI.error("You must be signed in to perform that action.");
            return null;
        }
        if (!u.isActive()) {
            ConsoleUI.error("Your account is deactivated. Contact a librarian.");
            return null;
        }
        return u;
    }

    // —— HELPERS ——

    private static boolean userExists(String username) {
        try (BufferedReader r = Files.newBufferedReader(USER_FILE_PATH)) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.startsWith(username + ",")) return true;
            }
        } catch (IOException ignored) {}
        return false;
    }

    private static String authenticate(String username, String password) {
        if (!Files.exists(USER_FILE_PATH)) {
            ConsoleUI.warning("User file not found. Please signup first.");
            return null;
        }

        try (BufferedReader r = Files.newBufferedReader(USER_FILE_PATH)) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] parts = line.strip().split(",");
                if (parts.length >= 4) {
                    String storedUser = parts[0];
                    String storedPass = parts[1];
                    String role = parts[2];
                    boolean isActive = Boolean.parseBoolean(parts[3]);

                    if (storedUser.equals(username) && storedPass.equals(password)) {
                        if (!isActive) {
                            ConsoleUI.error("Account is suspended. Please contact a librarian.");
                            return null;
                        }
                        return role;
                    }
                }
            }
        } catch (IOException e) {
            ConsoleUI.warning("Failed to read user data: " + e.getMessage());
        }

        return null;
    }

}
