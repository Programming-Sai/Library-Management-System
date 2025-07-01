package org.ebenlib.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public class AuthHandler {

    private static final Path USER_FILE_PATH = Paths.get(
        "src", "main", "resources", "users.csv"
    );
    private static final Path SESSION_FILE_PATH = Paths.get(
        "src", "main", "resources", "session.txt"
    );

    public static void handle(String[] args, Map<String, String> opts) {
        if (args.length < 2) {
            printHelp();
            return;
        }
        String action = args[1];
        switch (action) {
            case "signin":
                if (guardNoActiveSession()) handleSignin(opts);
                break;
            case "signup":
                if (guardNoActiveSession()) handleSignup(opts);
                break;
            case "signout":
                handleSignout();
                break;
            default:
                System.out.println("❌ Unknown auth action: " + action);
                printHelp();
        }
    }

    private static void printHelp() {
        System.out.println("📚 Usage: ebenlib auth [signin|signup|signout] [options]");
        System.out.println("  signin  -u <username> -p <password>       (or --username, --password)");
        System.out.println("  signup  -u <username> -p <password> -cp <confirmPassword> [-r <role>]");
        System.out.println("  signout                                   Sign out of current session");
    }

    private static void handleSignin(Map<String, String> opts) {
        String user = firstNonNull(opts.get("u"), opts.get("username"));
        String pass = firstNonNull(opts.get("p"), opts.get("password"));
        if (user == null || pass == null) {
            System.out.println("❌ Both -u/--username and -p/--password are required.");
            return;
        }
        String role = authenticate(user, pass);
        if (role != null) {
            saveSession(user, role);
            System.out.printf("✅ Signin successful! Welcome, %s (%s)%n", user, role);
        } else {
            System.out.println("❌ Signin failed: Invalid credentials.");
        }
    }

    private static void handleSignup(Map<String, String> opts) {
        String user = firstNonNull(opts.get("u"), opts.get("username"));
        String pass = firstNonNull(opts.get("p"), opts.get("password"));
        String cp   = firstNonNull(opts.get("cp"), opts.get("confirm-password"));
        String role = firstNonNull(opts.get("r"), opts.get("role"));
        if (role == null) role = "Reader";

        if (user == null || pass == null || cp == null) {
            System.out.println("❌ -u/--username, -p/--password and -cp/--confirm-password are required.");
            return;
        }
        if (!pass.equals(cp)) {
            System.out.println("❌ Password and confirm-password do not match.");
            return;
        }
        if (!role.equals("Reader") && !role.equals("Librarian")) {
            System.out.println("❌ Role must be either 'Reader' or 'Librarian'.");
            return;
        }
        try {
            Files.createDirectories(USER_FILE_PATH.getParent());
            if (!Files.exists(USER_FILE_PATH)) Files.createFile(USER_FILE_PATH);
        } catch (IOException ignored) {}
        if (userExists(user)) {
            System.out.println("❌ That username is already taken.");
            return;
        }
        try (BufferedWriter writer = Files.newBufferedWriter(USER_FILE_PATH, StandardOpenOption.APPEND)) {
            writer.write(String.join(",", user, pass, role));
            writer.newLine();
            saveSession(user, role);
            System.out.printf("✅ Signup successful! You can now signin as %s (%s)%n", user, role);
        } catch (IOException e) {
            System.out.println("⚠️ Failed to register user: " + e.getMessage());
        }
    }

    // —— SIGNOUT ——
    private static void handleSignout() {
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

    public static String getCurrentUser() {
        if (!Files.exists(SESSION_FILE_PATH)) return null;
        try (BufferedReader reader = Files.newBufferedReader(SESSION_FILE_PATH)) {
            String line = reader.readLine();
            return line != null ? line.split(",")[0] : null;
        } catch (IOException e) {
            return null;
        }
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

    private static String firstNonNull(String... values) {
        for (String v : values) if (v != null) return v;  return null;
    }
}
