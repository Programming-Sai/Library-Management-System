package org.ebenlib.borrow;

import org.ebenlib.cli.ConsoleUI;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class BorrowSettings {
    public static int    loanPeriodDays       = 14;
    public static double finePerDay           = 1.5;
    public static double fineBlockThreshold   = 10.0;

    /** Call once at app startup if you want to override defaults from a file */
    public static void loadSettings(String path) {
        try (BufferedReader r = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = r.readLine()) != null) {
                if      (line.startsWith("loan_period_days="))
                    loanPeriodDays     = Integer.parseInt(line.split("=")[1].trim());
                else if (line.startsWith("fine_per_day="))
                    finePerDay         = Double.parseDouble(line.split("=")[1].trim());
                else if (line.startsWith("fine_block_threshold="))
                    fineBlockThreshold = Double.parseDouble(line.split("=")[1].trim());
            }
        } catch (IOException e) {
            ConsoleUI.error("Error loading borrow settings: " + e.getMessage());
        }
    }
}
    