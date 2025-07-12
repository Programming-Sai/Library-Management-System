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

    private static void handleList() {
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

    private static void handleDelete(Map<String,String> o) {
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

    private static void handleRoleChange(Map<String,String> o, String newRole) {
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

    private static void handleActivation(Map<String,String> o, boolean activate) {
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
}
