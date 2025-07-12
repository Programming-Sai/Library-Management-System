package org.ebenlib.cli;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.ebenlib.book.BookHandler;
import org.ebenlib.borrow.BorrowHandler;

public class InteractiveMenus {

    public static final Set<String> NAVIGATION_COMMANDS = Set.of(
        "User", "Book", "Borrow", "Profile", "System", "Report"
    );


    public static Map<String, Runnable> getMainMenu(String role, String username) {
        Map<String, Runnable> menu = new LinkedHashMap<>();
        if (role.equalsIgnoreCase("Librarian")){
            menu.put("User",     () -> runWithPause(() -> new InteractiveShell(username, role).run(getUserMenu(role))));
            menu.put("System",   () -> runWithPause(() -> new InteractiveShell(username, role).run(getSystemMenu(role))));
            menu.put("Report",   () -> runWithPause(() -> new InteractiveShell(username, role).run(getReportMenu(role))));
        }
        menu.put("Book",     () -> runWithPause(() -> new InteractiveShell(username, role).run(getBookMenu(role))));
        menu.put("Borrow",   () -> runWithPause(() -> new InteractiveShell(username, role).run(getBorrowMenu(role))));
        menu.put("Profile",  () -> runWithPause(() -> new InteractiveShell(username, role).run(getProfileMenu(role))));
        menu.put("Logout",   () -> runWithPause(AuthHandler::handleSignout));
        menu.put("Exit",     () -> System.exit(0));
        return menu;
    }

    public static Map<String, Runnable> getUnauthenticatedMenu() {
        Map<String, Runnable> menu = new LinkedHashMap<>();
        menu.put("Sign In",  () -> runWithPause(() -> AuthHandler.interactiveSignin(true)));
        menu.put("Sign Up",  () -> runWithPause(() -> AuthHandler.interactiveSignup(true)));
        menu.put("Exit",     () -> System.exit(0));
        return menu;
    }

    public static Map<String, Runnable> getUserMenu(String role) {
        Map<String, Runnable> menu = new LinkedHashMap<>();
        if (role.equalsIgnoreCase("Librarian")) {
            menu.put("List Users",   () -> runWithPause(() -> stub("List Users")));
            menu.put("Promote User", () -> runWithPause(() -> stub("Promote User")));
        }
        menu.put("Back",   () -> {});
        menu.put("Exit",   () -> System.exit(0));
        return menu;
    }

    public static Map<String, Runnable> getBookMenu(String role) {
        Map<String, Runnable> menu = new LinkedHashMap<>();
        if (role.equalsIgnoreCase("Librarian")) {
            menu.put("Add Book",    () -> runWithPause(BookHandler::interactiveAdd));
            menu.put("Delete Book", () -> runWithPause(BookHandler::interactiveDelete));
            menu.put("Update Book", () -> runWithPause(BookHandler::interactiveUpdate));
            menu.put("Stats and Analytics", () -> runWithPause(BookHandler::interactiveStats));
        }
        menu.put("List Books", () -> runWithPause(BookHandler::handleList));
        menu.put("Search Books", () -> runWithPause(() -> BookHandler.interactiveSearch()));
        menu.put("Back",       () -> {});
        menu.put("Exit",       () -> System.exit(0));
        return menu;
    }

    public static Map<String, Runnable> getBorrowMenu(String role) {
        Map<String, Runnable> menu = new LinkedHashMap<>();
        if (role.equalsIgnoreCase("Librarian")) {
            menu.put("List All Pending Borrow Requests",    () -> runWithPause(() -> BorrowHandler.handleList(Map.of("status","PENDING"))));
            menu.put("List All Borrow Operations",    () -> runWithPause(() -> BorrowHandler.handleHistory(Map.of(), true)));
            menu.put("Approve Borrow Request",    () -> runWithPause(() -> BorrowHandler.interactiveApproveReject()));
            menu.put("Reject Borrow Request",    () -> runWithPause(() -> BorrowHandler.interactiveApproveReject()));
        }   
        menu.put("Request Book To Borrow",   () -> runWithPause(() -> BorrowHandler.interactiveRequest()));
        menu.put("Return Book",    () -> runWithPause(() -> BorrowHandler.handleReturnInteractive()));
        menu.put("My Borrow History",() -> runWithPause( () -> BorrowHandler.handleHistory(Map.of(), false)));
        menu.put("Back",           () -> {});
        menu.put("Exit",           () -> System.exit(0));
        return menu;
    }

    public static Map<String, Runnable> getProfileMenu(String role) {
        Map<String, Runnable> menu = new LinkedHashMap<>();
        menu.put("View Profile",    () -> runWithPause(() -> stub("View Profile")));
        menu.put("Update Profile",  () -> runWithPause(() -> stub("Update Profile")));
        menu.put("Change Password", () -> runWithPause(() -> stub("Change Password")));
        menu.put("Back",            () -> {});
        menu.put("Exit",            () -> System.exit(0));
        return menu;
    }

    public static Map<String, Runnable> getSystemMenu(String role) {
        Map<String, Runnable> menu = new LinkedHashMap<>();
        if (role.equalsIgnoreCase("Librarian")) {
            menu.put("System Stats",  () -> runWithPause(() -> stub("System Stats")));
            menu.put("Overdue Items", () -> runWithPause(() -> stub("Overdue Items")));
        }
        menu.put("Back",  () -> {});
        menu.put("Exit",  () -> System.exit(0));
        return menu;
    }

    public static Map<String, Runnable> getReportMenu(String role) {
        Map<String, Runnable> menu = new LinkedHashMap<>();
        if (role.equalsIgnoreCase("Librarian")) {
            menu.put("Generate Report", () -> runWithPause(() -> stub("Generate Report")));
        }
        menu.put("Back",  () -> {});
        menu.put("Exit",  () -> System.exit(0));
        return menu;
    }

    private static void stub(String action) {
        ConsoleUI.info("[Stub] " + action);
        // Do not press any key here — `runWithPause()` handles it
    }

    public static void runWithPause(Runnable action) {
        action.run();
        ConsoleUI.pressEnterToContinue();
    }
}
