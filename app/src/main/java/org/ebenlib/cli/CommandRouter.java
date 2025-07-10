package org.ebenlib.cli;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ebenlib.book.BookHandler;
import org.ebenlib.borrow.BorrowHandler;
import org.ebenlib.cli.ConsoleUI.Colorizer;

public class CommandRouter {

    public static void route(String[] args) {
        if (args.length == 0) {
            printHelp();
            return;
        }

        String command = args[0];
        Map<String, String> options = parseOptions(args);
        String currentUserRole;
        if (AuthHandler.getCurrentUser() != null){
            currentUserRole = AuthHandler.getCurrentUser().getRole();
        }else{
            currentUserRole = "Reader";
        }
        switch (command) {
            case "--interactive":
                User userData = AuthHandler.getCurrentUser();
                InteractiveShell shell;

            // ── Welcome Screen (runs only once) ─────────────────────────
                List<String> gradientColors = Arrays.asList(
                    ConsoleUI.BLUE,
                    ConsoleUI.CYAN,
                    ConsoleUI.PURPLE
                );

                Colorizer slantedColorizer = ConsoleUI.gradientColorizer(gradientColors, 30.0);
                ConsoleUI.printLogo(slantedColorizer);
                System.out.println("\n");
                
        
                if (userData != null) {
                    ConsoleUI.success("Welcome back, " + userData.getUsername() + "!");
                    ConsoleUI.pressEnterToContinue();
                    ConsoleUI.breadCrumbs.push("Home");
                    shell = new InteractiveShell(userData.getUsername(), userData.getRole());
                    shell.run(InteractiveMenus.getMainMenu(userData.getRole(), userData.getUsername()));
                } else {
                    ConsoleUI.info("Welcome! Please sign in or sign up.");
                    ConsoleUI.pressEnterToContinue();
                    ConsoleUI.breadCrumbs.push("Home");
                    shell = new InteractiveShell("Guest", "Unauthenticated");
                    shell.run(InteractiveMenus.getUnauthenticatedMenu());
                }
                break;

            case "auth":
                AuthHandler.handle(args, options);
                break;

            case "user":
                if (!currentUserRole.equals("Librarian")) {
                    ConsoleUI.error("Only librarians can use this command.");
                    return;
                }

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
                BookHandler.handle(args, options);
                break;

            case "borrow":
                BorrowHandler.handle(args, options);
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
                if (!currentUserRole.equals("Librarian")) {
                    ConsoleUI.error("Only librarians can use this command.");
                    return;
                }
                // System management stubs
                if (args.length < 2) {
                    System.out.println("[SYSTEM] No action specified. Available: init, stats, overdue, report");
                } else {
                    System.out.println("[SYSTEM] Stub: system " + args[1] + " invoked with options " + options);
                }
                break;

            case "report":
                if (!currentUserRole.equals("Librarian")) {
                    ConsoleUI.error("Only librarians can use this command.");
                    return;
                }
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
            case "--help":
                printHelp();;
                break;
            case "-h":
                printHelp();;
                break;

            default:
                System.out.println("❌ Unknown command: " + command);
                printHelp();
        }
    }

    private static void printHelp() {
            // build a 30° slanted gradient from BLUE → CYAN → PURPLE
            List<String> gradient = Arrays.asList(ConsoleUI.BLUE, ConsoleUI.CYAN, ConsoleUI.PURPLE);
            Colorizer colorFn = ConsoleUI.gradientColorizer(gradient, 30);

            // render our ASCII logo in gradient
            ConsoleUI.printLogo(colorFn);
            ConsoleUI.header(" EbenLib CLI — Command Reference ");

            ConsoleUI.println("Usage:", ConsoleUI.BOLD);
            ConsoleUI.println("  ebenlib <command> [options]\n", ConsoleUI.WHITE);

            ConsoleUI.println("Available Commands:", ConsoleUI.UNDERLINE);

            ConsoleUI.println("  --interactive", ConsoleUI.BRIGHT_CYAN);
            ConsoleUI.println("      Launch interactive TUI mode\n", ConsoleUI.WHITE);

            ConsoleUI.println("  auth [signin|signup|signout]", ConsoleUI.BRIGHT_CYAN);
            ConsoleUI.println("      signin    Sign in", ConsoleUI.WHITE);
            ConsoleUI.println("      signup    Register", ConsoleUI.WHITE);
            ConsoleUI.println("      signout   Logout\n", ConsoleUI.WHITE);

            ConsoleUI.println("  user [list|delete|promote|demote|suspend|activate]", ConsoleUI.BRIGHT_CYAN);
            ConsoleUI.println("      list      List users", ConsoleUI.WHITE);
            ConsoleUI.println("      delete    Remove a user", ConsoleUI.WHITE);
            ConsoleUI.println("      promote   Make user a Librarian", ConsoleUI.WHITE);
            ConsoleUI.println("      demote    Revoke Librarian role", ConsoleUI.WHITE);
            ConsoleUI.println("      suspend   Disable account", ConsoleUI.WHITE);
            ConsoleUI.println("      activate  Re-enable account\n", ConsoleUI.WHITE);

            ConsoleUI.println("  book [add|update|delete|list|search|stats]", ConsoleUI.BRIGHT_CYAN);
            ConsoleUI.println("      add       Add new book", ConsoleUI.WHITE);
            ConsoleUI.println("      update    Edit book details", ConsoleUI.WHITE);
            ConsoleUI.println("      delete    Delete book", ConsoleUI.WHITE);
            ConsoleUI.println("      stats     Book analytics\n", ConsoleUI.WHITE);
            ConsoleUI.println("      list      Show inventory", ConsoleUI.WHITE);
            ConsoleUI.println("      search    Find books", ConsoleUI.WHITE);

            ConsoleUI.println("  borrow [request|approve|reject|return|list|history|all-history]", ConsoleUI.BRIGHT_CYAN);
            ConsoleUI.println("      request        Borrow a book", ConsoleUI.WHITE);
            ConsoleUI.println("      list           Active requests", ConsoleUI.WHITE);
            ConsoleUI.println("      all-history    Past transactions of all users.", ConsoleUI.WHITE);
            ConsoleUI.println("      approve        Approve request", ConsoleUI.WHITE);
            ConsoleUI.println("      reject         Reject request", ConsoleUI.WHITE);
            ConsoleUI.println("      return         Return a book", ConsoleUI.WHITE);
            ConsoleUI.println("      history        Past transactions\n", ConsoleUI.WHITE);

            ConsoleUI.println("  profile [view|update|password]", ConsoleUI.BRIGHT_CYAN);
            ConsoleUI.println("      view      Show your profile", ConsoleUI.WHITE);
            ConsoleUI.println("      update    Change username", ConsoleUI.WHITE);
            ConsoleUI.println("      password  Change password\n", ConsoleUI.WHITE);

            ConsoleUI.println("  system [init|stats|overdue|report]", ConsoleUI.BRIGHT_CYAN);
            ConsoleUI.println("      init      Bootstrap/reset system", ConsoleUI.WHITE);
            ConsoleUI.println("      stats     Show system metrics", ConsoleUI.WHITE);
            ConsoleUI.println("      overdue   List overdue items", ConsoleUI.WHITE);
            ConsoleUI.println("      report    Generate reports\n", ConsoleUI.WHITE);

            ConsoleUI.println("  report [top-borrowed|top-fines|category-distribution]", ConsoleUI.BRIGHT_CYAN);
            ConsoleUI.println("      top-borrowed            Most borrowed books", ConsoleUI.WHITE);
            ConsoleUI.println("      top-fines               Highest fines owed", ConsoleUI.WHITE);
            ConsoleUI.println("      category-distribution   Inventory by category\n", ConsoleUI.WHITE);

            ConsoleUI.println("  test", ConsoleUI.BRIGHT_CYAN);
            ConsoleUI.println("      Run the built‑in UI demo & tests\n", ConsoleUI.WHITE);

            ConsoleUI.println("  -h, --help", ConsoleUI.BRIGHT_CYAN);
            ConsoleUI.println("      Show this help screen\n", ConsoleUI.WHITE);

            ConsoleUI.header(" Examples ");
            ConsoleUI.println("  ebenlib auth signin", ConsoleUI.WHITE);
            ConsoleUI.println("  ebenlib book list",   ConsoleUI.WHITE);
            ConsoleUI.println("  ebenlib --interactive", ConsoleUI.WHITE);
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