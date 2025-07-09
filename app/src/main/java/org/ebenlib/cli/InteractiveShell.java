package org.ebenlib.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.ebenlib.cli.ConsoleUI.Colorizer;

public class InteractiveShell {
    private final String username;
    private final String role;
    public static final Scanner scanner = new Scanner(System.in);

    public InteractiveShell(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public void run(Map<String, Runnable> menu) {
        // ── Welcome Screen (runs only once) ─────────────────────────
        clearScreen();
        List<String> gradientColors = Arrays.asList(
            ConsoleUI.BLUE,
            ConsoleUI.CYAN,
            ConsoleUI.PURPLE
        );

        Colorizer slantedColorizer = ConsoleUI.gradientColorizer(gradientColors, 30.0);
        ConsoleUI.printLogo(slantedColorizer);
        System.out.println("\n");
        if (!"Guest".equals(username)) {
            ConsoleUI.success("Welcome back, " + username + "!");
        } else {
            ConsoleUI.info("Welcome! Please sign in or sign up.");
        }
        ConsoleUI.prompt("Press ENTER to continue");
        
        // ── Main Menu Loop ───────────────────────────────────────────
        while (true) {
            clearScreen();
            ConsoleUI.header("🕹️  " + username + " (" + role + ") — Select an option");
            List<String> options = new ArrayList<>(menu.keySet());
            for (int i = 0; i < options.size(); i++) {
                // number in bright cyan, text in bold white
                ConsoleUI.print("  " + (i + 1) + ". ", ConsoleUI.BRIGHT_CYAN);
                ConsoleUI.println(options.get(i), ConsoleUI.BOLD + ConsoleUI.WHITE);
            }

            String input = ConsoleUI.prompt(">>");
            int choice;
            try {
                choice = Integer.parseInt(input);
                if (choice < 1 || choice > options.size()) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                ConsoleUI.error("Invalid selection.");
                continue;
            }

            String selected = options.get(choice - 1);
            ConsoleUI.success("You selected: " + selected);
            menu.get(selected).run();

            if (selected.equalsIgnoreCase("Logout") ||
                selected.equalsIgnoreCase("Back")    ||
                selected.equalsIgnoreCase("Exit")) {
                break;
            }
        }
    }

    public static void clearScreen() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (IOException | InterruptedException ex) {
            // fallback: just print blank lines
            for (int i = 0; i < 50; i++) System.out.println();
        }
    }
}
