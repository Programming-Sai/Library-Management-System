package org.ebenlib.borrow;

import org.ebenlib.cli.ConsoleUI;

import java.io.BufferedReader;
// import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class BorrowSettings {

    public static int    loanPeriodDays       = 14;
    public static double finePerDay           = 1.5;
    public static double fineBlockThreshold   = 10.0;
    public static int lowStockThreshold = 10;

    static {
        // System.out.println("Testing");
        // loadSettings(Paths.get("app", "src", "main", "resources", "settings.txt"));
    }

    /** Call once at app startup if you want to override defaults from a file */
    public static void loadSettings(Path folder) {
        Path path = (folder == null) ? Path.of("resources", "settings.txt") : folder;

        if (!Files.exists(path)) return;

        try (BufferedReader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null) {
                if      (line.startsWith("loan_period_days="))
                    loanPeriodDays     = Integer.parseInt(line.split("=")[1].trim());
                else if (line.startsWith("fine_per_day="))
                    finePerDay         = Double.parseDouble(line.split("=")[1].trim());
                else if (line.startsWith("fine_block_threshold="))
                    fineBlockThreshold = Double.parseDouble(line.split("=")[1].trim());
                else if (line.startsWith("low_stock_threshold="))
                    lowStockThreshold = Integer.parseInt(line.split("=")[1].trim());
            }
        } catch (IOException e) {
            ConsoleUI.error("Error loading borrow settings: " + e.getMessage());
        }
    }

    public static void printSettings() {
        ConsoleUI.println("Current Settings:", ConsoleUI.BOLD);
        ConsoleUI.println("  loanPeriod        = " + loanPeriodDays + " days", ConsoleUI.WHITE);
        ConsoleUI.println("  finePerDay        = ₵" + finePerDay, ConsoleUI.WHITE);
        ConsoleUI.println("  blockThreshold    = ₵" + fineBlockThreshold, ConsoleUI.WHITE);
        ConsoleUI.println("  lowStock          = " + lowStockThreshold, ConsoleUI.WHITE);
    }


    public static boolean set(String key, String value) {
        try {
            switch (key.toLowerCase()) {
                case "loanperiod":
                    loanPeriodDays = Integer.parseInt(value);
                    break;
                case "fineperday":
                    finePerDay = Double.parseDouble(value);
                    break;
                case "blockthreshold":
                    fineBlockThreshold = Double.parseDouble(value);
                    break;
                case "lowstock":
                    lowStockThreshold = Integer.parseInt(value);
                    break;
                default:
                    ConsoleUI.error("Unknown setting.");
                    return false;
            }
            save();
            return true;
        } catch (Exception e) {
            ConsoleUI.error("Invalid value for setting.");
            return false;
        }
    }


    public static void save() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("loan_period_days=").append(loanPeriodDays).append("\n");
            sb.append("fine_per_day=").append(finePerDay).append("\n");
            sb.append("fine_block_threshold=").append(fineBlockThreshold).append("\n");
            sb.append("low_stock_threshold=").append(lowStockThreshold).append("\n");

            Path path = Path.of("app", "src", "main", "resources", "settings.txt");
            Files.writeString(path, sb.toString(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            ConsoleUI.error("Failed to save settings: " + e.getMessage());
        }
    }

}
    