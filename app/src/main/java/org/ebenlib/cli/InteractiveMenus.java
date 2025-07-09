package org.ebenlib.cli;

import java.util.LinkedHashMap;
import java.util.Map;

public class InteractiveMenus {

    public static Map<String, Runnable> getMainMenu(String role, String username) {
        Map<String, Runnable> menu = new LinkedHashMap<>();
        menu.put("User",     () -> new InteractiveShell(username, role).run(getUserMenu(role)));
        menu.put("Book",     () -> new InteractiveShell(username, role).run(getBookMenu(role)));
        menu.put("Borrow",   () -> new InteractiveShell(username, role).run(getBorrowMenu(role)));
        menu.put("Profile",  () -> new InteractiveShell(username, role).run(getProfileMenu(role)));
        menu.put("System",   () -> new InteractiveShell(username, role).run(getSystemMenu(role)));
        menu.put("Report",   () -> new InteractiveShell(username, role).run(getReportMenu(role)));
        menu.put("Logout",   () -> AuthHandler.handleSignout());
        menu.put("Exit",     () -> System.exit(0));
        return menu;
    }

    public static Map<String, Runnable> getUnauthenticatedMenu() {
        Map<String, Runnable> menu = new LinkedHashMap<>();
        menu.put("Sign In",  () -> AuthHandler.interactiveSignin(true));
        menu.put("Sign Up",  () -> AuthHandler.interactiveSignup(true));
        menu.put("Exit",     () -> System.exit(0));
        return menu;
    }

    public static Map<String, Runnable> getUserMenu(String role) {
        Map<String, Runnable> menu = new LinkedHashMap<>();
        if (role.equalsIgnoreCase("Librarian")) {
            menu.put("List Users",   () -> stub("List Users"));
            menu.put("Promote User", () -> stub("Promote User"));
        }
        menu.put("Back",   () -> {/* no-op to go back */});
        menu.put("Exit",   () -> System.exit(0));
        return menu;
    }

    public static Map<String, Runnable> getBookMenu(String role) {
        Map<String, Runnable> menu = new LinkedHashMap<>();
        if (role.equalsIgnoreCase("Librarian")) {
            menu.put("Add Book",    () -> stub("Add Book"));
            menu.put("Delete Book", () -> stub("Delete Book"));
            menu.put("Update Book", () -> stub("Update Book"));
        }
        menu.put("List Books", () -> stub("List Books"));
        menu.put("Back",       () -> {});
        menu.put("Exit",       () -> System.exit(0));
        return menu;
    }

    public static Map<String, Runnable> getBorrowMenu(String role) {
        Map<String, Runnable> menu = new LinkedHashMap<>();
        if (role.equalsIgnoreCase("Librarian")) {
            menu.put("Approve Request", () -> stub("Approve Request"));
            menu.put("Reject Request",  () -> stub("Reject Request"));
        }
        menu.put("Request Book",  () -> stub("Request Book"));
        menu.put("Return Book",   () -> stub("Return Book"));
        menu.put("Borrow History",() -> stub("Borrow History"));
        menu.put("Back",          () -> {});
        menu.put("Exit",          () -> System.exit(0));
        return menu;
    }

    public static Map<String, Runnable> getProfileMenu(String role) {
        Map<String, Runnable> menu = new LinkedHashMap<>();
        menu.put("View Profile",    () -> stub("View Profile"));
        menu.put("Update Profile",  () -> stub("Update Profile"));
        menu.put("Change Password", () -> stub("Change Password"));
        menu.put("Back",            () -> {});
        menu.put("Exit",            () -> System.exit(0));
        return menu;
    }

    public static Map<String, Runnable> getSystemMenu(String role) {
        Map<String, Runnable> menu = new LinkedHashMap<>();
        if (role.equalsIgnoreCase("Librarian")) {
            menu.put("System Stats",   () -> stub("System Stats"));
            menu.put("Overdue Items",  () -> stub("Overdue Items"));
        }
        menu.put("Back",  () -> {});
        menu.put("Exit",  () -> System.exit(0));
        return menu;
    }

    public static Map<String, Runnable> getReportMenu(String role) {
        Map<String, Runnable> menu = new LinkedHashMap<>();
        if (role.equalsIgnoreCase("Librarian")) {
            menu.put("Generate Report", () -> stub("Generate Report"));
        }
        menu.put("Back",  () -> {});
        menu.put("Exit",  () -> System.exit(0));
        return menu;
    }

    private static void stub(String action) {
        ConsoleUI.info("[Stub] " + action);
        ConsoleUI.prompt("Press ENTER to return");
    }
}
