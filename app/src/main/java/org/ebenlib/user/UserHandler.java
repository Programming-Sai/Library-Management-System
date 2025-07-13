package org.ebenlib.user;

import org.ebenlib.cli.ConsoleUI;
import org.ebenlib.cli.TablePrinter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserHandler {

    private static final UserStore store = new UserStore();

    static {
        store.load();
    }

    public static void handle(String[] args, Map<String, String> opts) {
        if (args.length < 2) {
            printHelp();
            return;
        }

        switch (args[1].toLowerCase()) {
            case "list"     -> handleList();
            case "delete"   -> handleDelete(opts);
            case "promote"  -> handleRoleChange(opts, "Librarian");
            case "demote"   -> handleRoleChange(opts, "Reader");
            case "deactivate"  -> handleActivation(opts, false);
            case "activate" -> handleActivation(opts, true);
            default         -> printHelp();
        }

        store.save();
    }

    public static void printHelp() {
        ConsoleUI.header("User Management");
        ConsoleUI.println("  user list                                  List all users", ConsoleUI.WHITE);
        ConsoleUI.println("  user delete        --username <name>       Delete a user", ConsoleUI.WHITE);
        ConsoleUI.println("  user promote       --username <name>       Make user a Librarian", ConsoleUI.WHITE);
        ConsoleUI.println("  user demote        --username <name>       Revoke Librarian role", ConsoleUI.WHITE);
        ConsoleUI.println("  user deactivate    --username <name>       Suspend user account", ConsoleUI.WHITE);
        ConsoleUI.println("  user activate      --username <name>       Reactivate suspended user", ConsoleUI.WHITE);
    }

    public static void handleList() {
        List<User> all = store.listAll();
        if (all.isEmpty()) {
            ConsoleUI.info("No users available.");
            return;
        }

        // Build rows: Username | Role | Status
        List<String[]> rows = all.stream()
            .map(u -> new String[]{
                u.getUsername(),
                u.getRole(),
                u.isActive() ? "Active" : "Deactivated"
            })
            .collect(Collectors.toList());

        String[] headers = {"Username", "Role", "Status"};
        int[] widths = {20, 12, 12};
        TablePrinter.printHeader(headers, widths);
        TablePrinter.printTable(rows, 10, widths);
    }

    public static void handleDelete(Map<String,String> o) {
        String user = o.get("username");
        if (user == null) {
            ConsoleUI.error("Missing --username");
            return;
        }
        if (store.delete(user)) {
            ConsoleUI.success("Deleted user: " + user);
        } else {
            ConsoleUI.error("No such user: " + user);
        }
    }

    public static void handleRoleChange(Map<String,String> o, String newRole) {
        String user = o.get("username");
        if (user == null) {
            ConsoleUI.error("Missing --username");
            return;
        }
        if (store.changeRole(user, newRole)) {
            ConsoleUI.success("User " + user + " is now " + newRole);
        } else {
            ConsoleUI.error("No such user: " + user);
        }
    }

    public static void handleActivation(Map<String,String> o, boolean activate) {
        String user = o.get("username");
        if (user == null) {
            ConsoleUI.error("Missing --username");
            return;
        }
        if (store.setActive(user, activate)) {
            ConsoleUI.success("User " + user + " " + (activate ? "activated" : "suspended"));
        } else {
            ConsoleUI.error("No such user: " + user);
        }
    }


    public static String promptUsernameSelection() {
        List<User> users = store.listAll();
        if (users.isEmpty()) {
            ConsoleUI.error("No users found.");
            return null;
        }

        ConsoleUI.header("Select a User");
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            System.out.printf("%2d. %s (%s) [%s]%n", i + 1, u.getUsername(), u.getRole(),
                u.isActive() ? "Active" : "Suspended");
        }

        while (true) {
            String input = ConsoleUI.prompt("Enter number (or 0 to cancel):");
            try {
                int index = Integer.parseInt(input);
                if (index == 0) return null;
                if (index > 0 && index <= users.size()) {
                    return users.get(index - 1).getUsername();
                }
            } catch (NumberFormatException ignored) {}
            ConsoleUI.error("Invalid input. Please enter a number between 1 and " + users.size());
        }
    }



    public static void interactivePromote() {
        String user = promptUsernameSelection();
        handleRoleChange(Map.of("username", user), "Librarian");
        store.save(); 
    }

    public static void interactiveDemote() {
        String user = promptUsernameSelection();
        handleRoleChange(Map.of("username", user), "Reader");
        store.save(); 
    }

    public static void interactiveActivation(boolean activate) {
        String user = promptUsernameSelection();
        handleActivation(Map.of("username", user), activate);
        store.save(); 
    }

    public static void interactiveDelete() {
        String user = promptUsernameSelection();
        ConsoleUI.warning("Are you sure you want to delete " + user + "? This cannot be undone.");
        String confirm = ConsoleUI.prompt("Type 'yes' to confirm:");
        if (confirm.equalsIgnoreCase("yes")) {
            handleDelete(Map.of("username", user));
        } else {
            ConsoleUI.info("Delete cancelled.");
        }
        store.save(); 
    }


}
