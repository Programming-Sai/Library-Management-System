package org.ebenlib.cli;



import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class InteractiveShell {
    private final String username;
    private final String role;

    public InteractiveShell(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public void run(Map<String, Runnable> menu) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            clearScreen();
            System.out.println("\nPlease choose an option:");
            List<String> options = new ArrayList<>(menu.keySet());
            for (int i = 0; i < options.size(); i++) {
                System.out.printf("  %d. %s%n", i + 1, options.get(i));
            }
            System.out.print(">> ");
            String input = scanner.nextLine();

            int choice;
            try {
                choice = Integer.parseInt(input);
                if (choice < 1 || choice > options.size()) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid selection.\n");
                continue;
            }

            String selected = options.get(choice - 1);
            System.out.printf("✅ You selected: %s%n%n", selected);
            menu.get(selected).run();

            if (selected.equalsIgnoreCase("Logout") || selected.equalsIgnoreCase("Back")) {
                break;
            }
        }
    }

    public static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (IOException | InterruptedException ex) {
            // Fallback
            for (int i = 0; i < 50; i++) System.out.println();
        }
    }
}
