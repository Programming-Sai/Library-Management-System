package org.ebenlib.profile;


import org.ebenlib.cli.ConsoleUI;
import org.ebenlib.cli.TablePrinter;
import org.ebenlib.ds.EbenLibList;
import org.ebenlib.ds.EbenLibMap;
import org.ebenlib.user.User;
import org.ebenlib.user.UserStore;
import org.ebenlib.borrow.BorrowHandler;
import org.ebenlib.cli.AuthHandler;



public class ProfileHandler {

    public static void handle(String[] args, EbenLibMap<String, String> opts) {
        if (args.length < 2) {
            printHelp();    
            return;
        }

        User current = AuthHandler.getCurrentUser();
        if (current == null) {
            ConsoleUI.error("No active session found.");
            return;
        }

        switch (args[1]) {
            case "view" -> handleView(current);
            case "update" -> handleUpdate(current);
            case "password" -> handleChangePassword(current);
            default -> printHelp();
        }
    }

    private static void handleView(User current) {
        ConsoleUI.header("Your Profile");
        
        String[] row = {
            current.getUsername(),
            current.getRole(),
            current.isActive() ? "Active" : "Suspended"
        };

        TablePrinter.printHeader(new String[]{"Username", "Role", "Status"}, new int[]{20, 10, 10});
        EbenLibList<String[]> tempList = new EbenLibList<>();
        tempList.add(row);
        TablePrinter.printTable(tempList, 12, new int[]{20, 10, 10});
        ConsoleUI.info("Borrow History:");
        BorrowHandler.handleHistory(EbenLibMap.<String, String>empty(), false);
    }


    private static void handleUpdate(User current) {
        String newName = ConsoleUI.prompt("Enter new username:");
        String confirm = ConsoleUI.prompt("Confirm username:");
        if (!newName.equals(confirm)) {
            ConsoleUI.error("Usernames do not match.");
            return;
        }

        if (!UserStore.rename(current.getUsername(), newName)) {
            ConsoleUI.error("Failed to update username.");
            return;
        }

        AuthHandler.updateSessionUsername(newName);
        BorrowHandler.store.updateUsername(current.getUsername(), newName);
        ConsoleUI.success("Username updated to: " + newName);
    }

    private static void handleChangePassword(User current) {
        String old = ConsoleUI.prompt("Enter current password:");
        if (!UserStore.verifyPassword(current.getUsername(), old)) {
            ConsoleUI.error("Incorrect password.");
            return;
        }

        String newPwd = ConsoleUI.prompt("Enter new password:");
        String confirm = ConsoleUI.prompt("Confirm new password:");
        if (!newPwd.equals(confirm)) {
            ConsoleUI.error("Passwords do not match.");
            return;
        }

        if (UserStore.updatePassword(current.getUsername(), newPwd)) {
            ConsoleUI.success("Password updated.");
        } else {
            ConsoleUI.error("Failed to update password.");
        }
    }

    public static void printHelp() {
        ConsoleUI.header("Profile Management");
        ConsoleUI.println("  profile view              View profile", ConsoleUI.WHITE);
        ConsoleUI.println("  profile update            Change username", ConsoleUI.WHITE);
        ConsoleUI.println("  profile password          Change password", ConsoleUI.WHITE);
    }
}
