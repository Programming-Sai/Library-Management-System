package org.ebenlib.system;

import org.ebenlib.cli.AuthHandler;
import org.ebenlib.cli.ConsoleUI;
import org.ebenlib.utils.FileUtil;
import org.ebenlib.borrow.BorrowSettings;

import java.io.File;
// import java.io.IOException;
import java.util.Map;
// import java.util.Scanner;

public class SystemHandler {
    public static boolean DEV_MODE = true; // toggle off when packaging


    public static void handle(String[] args, Map<String, String> options, String username) {
        if (args.length < 2) {
            ConsoleUI.error("Missing system command.");
            return;
        }

        switch (args[1]) {
            case "seed" -> seedData(username);
            case "import" -> importData(options, username);
            case "export" -> exportData(options, username);
            case "config" -> handleConfig(args, options, username);
            default -> printHelp();
        }
    }

    public static void printHelp() {
        ConsoleUI.println("\nSystem Commands", ConsoleUI.BOLD);
        ConsoleUI.println("  These are administrative commands for managing data and configuration.\n", ConsoleUI.DIM);

        ConsoleUI.println("  system seed", ConsoleUI.BRIGHT_CYAN);
        ConsoleUI.println("      Wipe current data and seed demo data (dev mode only).", ConsoleUI.WHITE);
        ConsoleUI.println("      WARNING: Irreversible. Password confirmation required.", ConsoleUI.DIM);

        ConsoleUI.println("\n  system import --file <folderPath>", ConsoleUI.BRIGHT_CYAN);
        ConsoleUI.println("      Import data from the specified folder containing:", ConsoleUI.WHITE);
        ConsoleUI.println("          books.csv, users.csv, borrows.csv, session.csv", ConsoleUI.DIM);
        ConsoleUI.println("      WARNING: Overwrites current data. Password confirmation required.", ConsoleUI.DIM);

        ConsoleUI.println("\n  system export --file <folderPath>", ConsoleUI.BRIGHT_CYAN);
        ConsoleUI.println("      Export current data into the specified folder as CSV files.", ConsoleUI.WHITE);
        ConsoleUI.println("      Password confirmation required.", ConsoleUI.DIM);

        ConsoleUI.println("\n  system config view", ConsoleUI.BRIGHT_CYAN);
        ConsoleUI.println("      View current system settings such as loan period and fine rate.", ConsoleUI.WHITE);

        ConsoleUI.println("\n  system config set <setting> <value>", ConsoleUI.BRIGHT_CYAN);
        ConsoleUI.println("      Update a configuration value. Requires password.", ConsoleUI.WHITE);
        ConsoleUI.println("      Available settings: loanPeriod, finePerDay, blockThreshold, lowStock", ConsoleUI.DIM);

        System.out.println();
    }


    private static void seedData(String username) {
        if (!DEV_MODE) {
            ConsoleUI.error("Seed is only allowed in dev mode.");
            return;
        }
        if (!confirmIrreversible() || !AuthHandler.requirePassword(username)) return;

        FileUtil.writeDemoData();
        ConsoleUI.success("Demo data seeded.");
    }

    private static void importData(Map<String, String> options, String username) {
        if (!options.containsKey("file")) {
            ConsoleUI.error("Missing --file <folderPath>");
            return;
        }

        if (!confirmIrreversible() || !AuthHandler.requirePassword(username)) return;
        FileUtil.loadFromFolder(new File(options.get("file")));
        ConsoleUI.success("Data imported successfully.");
    }

    private static void exportData(Map<String, String> options, String username) {
        if (!options.containsKey("file")) {
            ConsoleUI.error("Missing --file <folderPath>");
            return;
        }

        if (!AuthHandler.requirePassword(username)) return;

        FileUtil.exportToFolder(new File(options.get("file")));
        ConsoleUI.success("Data exported successfully.");
    }

    private static void handleConfig(String[] args, Map<String, String> options, String username) {
        if (args.length < 3) {
            ConsoleUI.error("Missing config subcommand.");
            return;
        }

        switch (args[2]) {
            case "view" -> BorrowSettings.printSettings();

            case "set" -> {
                if (args.length < 5) {
                    ConsoleUI.error("Usage: system config set <setting> <value>");
                    return;
                }

                if (!AuthHandler.requirePassword(username)) return;

                String setting = args[3];
                String value = args[4];

                if (BorrowSettings.set(setting, value)) {
                    ConsoleUI.success("Setting updated.");
                } else {
                    ConsoleUI.error("Invalid setting.");
                }
            }

            default -> ConsoleUI.error("Unknown config subcommand.");
        }
    }

    private static boolean confirmIrreversible() {
        if (FileUtil.hasExistingData()) {
            ConsoleUI.info("This action will overwrite existing data.");
            ConsoleUI.print("Are you sure? (yes/no): ", ConsoleUI.WHITE);
            return ConsoleUI.scanner.nextLine().equalsIgnoreCase("yes");
        }
        return true;
    }

    public static void interactiveSeed() {
        if (!DEV_MODE) {
            ConsoleUI.error("Seeding is only available in development mode.");
            return;
        }
        if (FileUtil.hasExistingData()) {
            ConsoleUI.info("This will overwrite existing data.");
            if (!ConsoleUI.confirm("Proceed?")) return;
        }
        if (!AuthHandler.requirePassword(AuthHandler.getCurrentUser().getUsername())) return;
        FileUtil.writeDemoData();
        ConsoleUI.success("Demo data seeded.");
    }

    public static void interactiveImport() {
        if (!AuthHandler.requirePassword(AuthHandler.getCurrentUser().getUsername())) return;
        ConsoleUI.info("This will overwrite current data.");
        if (!ConsoleUI.confirm("Continue?")) return;

        String path = ConsoleUI.prompt("Enter folder path to import from");
        FileUtil.loadFromFolder(new File(path));
        ConsoleUI.success("Data imported.");
    }

    public static void interactiveExport() {
        if (!AuthHandler.requirePassword(AuthHandler.getCurrentUser().getUsername())) return;

        String path = ConsoleUI.prompt("Enter destination folder path for export");
        FileUtil.exportToFolder(new File(path));
        ConsoleUI.success("Data exported.");
    }

    public static void interactiveConfig() {
    ConsoleUI.println("Which setting would you like to update?", ConsoleUI.BOLD);
    ConsoleUI.println("  loanperiod, fineperday, blockthreshold, lowstock", ConsoleUI.DIM);

    String key = ConsoleUI.prompt("Setting name");
    String val = ConsoleUI.prompt("New value");

    if (!AuthHandler.requirePassword(AuthHandler.getCurrentUser().getUsername())) return;
    if (BorrowSettings.set(key, val)) {
        ConsoleUI.success("Setting updated.");
    } else {
        ConsoleUI.error("Failed to update setting.");
    }
}

}
