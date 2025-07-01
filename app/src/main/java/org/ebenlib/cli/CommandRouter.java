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
                // InteractiveCLI.run();
                System.out.println("Interactive Mode accessed");
                break;

            case "auth":
                AuthHandler.handle(args, options);
                System.out.println("Login Handler");
                break;

            // Future: case "add-book": BookHandler.handleAdd(options); break;

            default:
                System.out.println("❌ Unknown command: " + command);
                printHelp();
        }
    }

    private static void printHelp() {
        System.out.println("📚 EbenLib CLI Help:");
        System.out.println("  --interactive              Start interactive mode");
        System.out.println("  auth signin -u <user> -p <pass>        Signin to system");
        System.out.println("  auth signup -u <user> -p <pass> -cp <confirm> [-r <role>]  Register a new user");
        // Add more commands here as they’re implemented
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