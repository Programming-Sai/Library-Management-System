package org.ebenlib.cli;

// import java.io.IOException;
// import java.util.Arrays;
import java.util.Scanner;

import org.ebenlib.ds.EbenLibList;
import org.ebenlib.ds.EbenLibMap;

// import org.ebenlib.cli.ConsoleUI.Colorizer;

public class InteractiveShell {
    private final String username;
    private final String role;
    public static final Scanner scanner = new Scanner(System.in);

    public InteractiveShell(String username, String role) {
        this.username = username;
        this.role = role;
        // if (!ConsoleUI.breadCrumbs.isEmpty()) {
            // ConsoleUI.breadCrumbs.pop();
        // }
    }

    public void run(EbenLibMap<String, Runnable> menu) {
        // int x = 0;
        // ── Main Menu Loop ───────────────────────────────────────────
        while (true) {
            // if (x == 1) {ConsoleUI.prompt("Press ENTER to continue"); }
            // if (x < 1){x++;}
            ConsoleUI.clearScreen();
            ConsoleUI.showBreadCrumbs();
            ConsoleUI.header("🕹️  " + username + " (" + role + ") — Select an option");
            String[] keys = menu.keySet().toArray(new String[0]);
            EbenLibList<String> options = EbenLibList.from(keys);
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
            // System.out.println(selected + ", "+ menu.get(selected) + ", "+ menu);
            if (!selected.equalsIgnoreCase("Back") && !selected.equalsIgnoreCase("Exit") && !selected.equalsIgnoreCase(ConsoleUI.safePeekBreadcrumb()) && InteractiveMenus.NAVIGATION_COMMANDS.contains(selected)) {
                ConsoleUI.breadCrumbs.push(selected);
            }
            menu.get(selected).run();

            if (selected.equalsIgnoreCase("Logout") || selected.equalsIgnoreCase("Back") || selected.equalsIgnoreCase("Exit")) {
                if (!ConsoleUI.breadCrumbs.isEmpty()) {
                    ConsoleUI.breadCrumbs.pop();
                }
                break;
            }
        }
    }
}
