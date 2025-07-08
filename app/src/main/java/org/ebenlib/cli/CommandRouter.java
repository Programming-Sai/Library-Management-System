package org.ebenlib.cli;

import java.util.HashMap;
import java.util.Map;

public class CommandRouter {

    public static void route(String[] args) {
        if (args.length == 0) {
            printHelp();
            return;
        }

        String command = args[0];
        Map<String, String> options = parseOptions(args);

        switch (command) {
            case "--interactive":
                User userData = AuthHandler.getCurrentUser();
                InteractiveShell shell;
                if (userData != null) {
                    shell = new InteractiveShell(userData.getUsername(), userData.getRole());
                    shell.run(InteractiveMenus.getMainMenu(userData.getRole(), userData.getUsername()));
                } else {
                    shell = new InteractiveShell("Guest", "Unauthenticated");
                    shell.run(InteractiveMenus.getUnauthenticatedMenu());
                }
                break;

            case "auth":
                AuthHandler.handle(args, options);
                break;

            case "user":
                // User management stubs
                if (args.length < 2) {
                    System.out.println("[USER] No action specified. Available: list, delete, promote, demote, suspend, activate");
                } else {
                    System.out.println("[USER] Stub: user " + args[1] + " invoked with options " + options);
                }
                break;

            case "book":
                // Book management stubs
                if (args.length < 2) {
                    System.out.println("[BOOK] No action specified. Available: add, delete, update, list, search, stats");
                } else {
                    System.out.println("[BOOK] Stub: book " + args[1] + " invoked with options " + options);
                }
                break;

            case "borrow":
                // Borrowing management stubs
                if (args.length < 2) {
                    System.out.println("[BORROW] No action specified. Available: request, approve, reject, return, list, history");
                } else {
                    System.out.println("[BORROW] Stub: borrow " + args[1] + " invoked with options " + options);
                }
                break;

            case "profile":
                // Profile management stubs
                if (args.length < 2) {
                    System.out.println("[PROFILE] No action specified. Available: view, update, password");
                } else {
                    System.out.println("[PROFILE] Stub: profile " + args[1] + " invoked with options " + options);
                }
                break;

            case "system":
                // System management stubs
                if (args.length < 2) {
                    System.out.println("[SYSTEM] No action specified. Available: init, stats, overdue, report");
                } else {
                    System.out.println("[SYSTEM] Stub: system " + args[1] + " invoked with options " + options);
                }
                break;

            case "report":
                // Reports & data analysis stubs
                if (args.length < 2) {
                    System.out.println("[REPORT] No action specified. Available: top-borrowed, top-fines, category-distribution");
                } else {
                    System.out.println("[REPORT] Stub: report " + args[1] + " invoked with options " + options);
                }
                break;
            case "test":
                ConsoleThemeTest.main(args);
                break;

            default:
                System.out.println("❌ Unknown command: " + command);
                printHelp();
        }
    }

    private static void printHelp() {
        System.out.println("📚 EbenLib CLI Help:");
        System.out.println("  --interactive                      Enter interactive mode");
        System.out.println("  auth    [signin|signup|signout]    User authentication");
        System.out.println("  user    [list|delete|promote...]   User management (Admin)");
        System.out.println("  book    [add|delete|update|list...] Book management (Librarian)");
        System.out.println("  borrow  [request|return|list|history] Borrowing system");
        System.out.println("  profile [view|update|password]     Account profile actions");
        System.out.println("  system  [init|stats]               System-level commands");
    }

    // Parse both short (-u val) and long (--username=val) style arguments
    private static Map<String, String> parseOptions(String[] args) {
        Map<String, String> options = new HashMap<>();
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--") && arg.contains("=")) {
                String[] parts = arg.substring(2).split("=", 2);
                options.put(parts[0], parts[1]);
            } else if (arg.startsWith("-") && arg.contains("=")) {
                String[] parts = arg.substring(1).split("=", 2);
                options.put(parts[0], parts[1]);
            } else if (arg.startsWith("-")) {
                String key = arg.startsWith("--") ? arg.substring(2) : arg.substring(1);
                // look ahead for value
                if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    options.put(key, args[++i]);
                }
            }
        }
        return options;
    }
}