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
import java.util.Scanner;

public class AuthHandler {

    private static final Path USER_FILE_PATH = Paths.get(
        "app", "src", "main", "resources", "users.csv"
    );
    private static final Path SESSION_FILE_PATH = Paths.get(
        "app", "src", "main", "resources", "session.txt"
    );
    private static final Scanner fallbackScanner = new Scanner(System.in);

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
                System.out.println("❌ Unknown auth action: " + action);
                printHelp();
        }
    }

    public static void printHelp() {
        System.out.println("📚 Usage: ebenlib auth [signin|signup|signout]");
        System.out.println("  signin   Prompt for username/password to log in");
        System.out.println("  signup   Prompt for username, password, confirm and role to register");
        System.out.println("  signout  Sign out of current session");
    }

    // —— INTERACTIVE SIGNIN ——
    public static void interactiveSignin(boolean isInteractive) {
    Console console = System.console();
    String user;
    char[] pwdChars;

    try {
        if (console != null) {
            user = console.readLine("Username: ");
            pwdChars = console.readPassword("Password: ");
        } else {
            System.out.print("Username: ");
            if (!fallbackScanner.hasNextLine()) {
                System.out.println("❌ No input available.");
                return;
            }
            user = fallbackScanner.nextLine();

            System.out.print("Password: ");
            if (!fallbackScanner.hasNextLine()) {
                System.out.println("❌ No input available.");
                return;
            }
            pwdChars = fallbackScanner.nextLine().toCharArray();
        }

        String pass = new String(pwdChars);
        String role = authenticate(user, pass);
        if (role != null) {
            saveSession(user, role);
            System.out.printf("✅ Signin successful! Welcome, %s (%s)%n", user, role);
             if (isInteractive) {
                InteractiveShell.clearScreen();
                new InteractiveShell(user, role).run(InteractiveMenus.getMainMenu(role, user));
            }
        } else {
            System.out.println("❌ Signin failed: Invalid credentials.");
        }
    } catch (Exception e) {
        System.out.println("❌ Input error: " + e.getMessage());
    }
}


    // —— INTERACTIVE SIGNUP ——
    public static void interactiveSignup(boolean isInteractive) {
        Console console = System.console();
        String user;
        char[] pwd1, pwd2;
        String role;
        String roleInput;

        if (console != null) {
            user = console.readLine("Choose a username: ");
            pwd1 = console.readPassword("Choose a password: ");
            pwd2 = console.readPassword("Confirm password: ");

            System.out.println("Choose role:");
            System.out.println("  1. Librarian");
            System.out.println("  2. Reader");
            System.out.print(">> ");
            roleInput = console.readLine(">> ");
        } else {
            System.out.print("Choose a username: ");
            user = fallbackScanner.nextLine();
            System.out.print("Choose a password: ");
            pwd1 = fallbackScanner.nextLine().toCharArray();
            System.out.print("Confirm password: ");
            pwd2 = fallbackScanner.nextLine().toCharArray();

            System.out.println("Choose role:");
            System.out.println("  1. Librarian");
            System.out.println("  2. Reader");
            System.out.print(">> ");
            roleInput = fallbackScanner.nextLine();
        }

        if (!new String(pwd1).equals(new String(pwd2))) {
            System.out.println("❌ Passwords do not match.");
            return;
        }

        if (roleInput == null) roleInput = "";

        switch (roleInput.strip()) {
            case "1" -> role = "Librarian";
            case "2", "" -> role = "Reader"; // default
            default -> {
                System.out.println("❌ Invalid selection. Please choose 1 or 2.");
                return;
            }
        }

        if (userExists(user)) {
            System.out.println("❌ That username is already taken.");
            return;
        }

        // Persist new user
        try {
            Files.createDirectories(USER_FILE_PATH.getParent());
            if (!Files.exists(USER_FILE_PATH)) Files.createFile(USER_FILE_PATH);
            try (BufferedWriter writer = Files.newBufferedWriter(USER_FILE_PATH, StandardOpenOption.APPEND)) {
                writer.write(String.join(",", user, new String(pwd1), role));
                writer.newLine();
            }
            saveSession(user, role);
            System.out.printf("✅ Signup successful! You are now logged in as %s (%s)%n", user, role);
             if (isInteractive) {
                InteractiveShell.clearScreen();
                new InteractiveShell(user, role).run(InteractiveMenus.getMainMenu(role, user));
            }
        } catch (IOException e) {
            System.out.println("⚠️ Failed to register user: " + e.getMessage());
        }
    }


    // —— SIGNOUT ——
    public static void handleSignout() {
        if (!Files.exists(SESSION_FILE_PATH)) {
            System.out.println("❌ No active session.");
            return;
        }
        try {
            Files.delete(SESSION_FILE_PATH);
            System.out.println("👋 You have been signed out.");
        } catch (IOException e) {
            System.out.println("⚠️ Failed to clear session: " + e.getMessage());
        }
    }

    private static boolean guardNoActiveSession() {
        if (Files.exists(SESSION_FILE_PATH)) {
            System.out.println("❌ A user is already signed in. Please sign out first.");
            return false;
        }
        return true;
    }

    private static void saveSession(String username, String role) {
        try (BufferedWriter writer = Files.newBufferedWriter(
                 SESSION_FILE_PATH, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write(username + "," + role);
        } catch (IOException e) {
            System.out.println("⚠️ Failed to save session: " + e.getMessage());
        }
    }

    public static User getCurrentUser() {
        if (!Files.exists(SESSION_FILE_PATH)) return null;

        try (BufferedReader reader = Files.newBufferedReader(SESSION_FILE_PATH)) {
            String line = reader.readLine();
            if (line != null) {
                String[] parts = line.strip().split(",");
                if (parts.length == 2) {
                    return new User(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            // Log or handle error if needed
        }

        return null;
    }


    private static boolean userExists(String username) {
        try (BufferedReader reader = Files.newBufferedReader(USER_FILE_PATH)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(username + ",")) return true;
            }
        } catch (IOException ignored) {}
        return false;
    }

    private static String authenticate(String username, String password) {
        if (!Files.exists(USER_FILE_PATH)) {
            System.out.println("⚠️ User Not Found. Please signup first.");
            return null;
        }
        try (BufferedReader reader = Files.newBufferedReader(USER_FILE_PATH)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.strip().split(",");
                if (parts.length == 3 && parts[0].equals(username) && parts[1].equals(password)) {
                    return parts[2];
                }
            }
        } catch (IOException e) {
            System.out.println("⚠️ Failed to read user data: " + e.getMessage());
        }
        return null;
    }
} 




