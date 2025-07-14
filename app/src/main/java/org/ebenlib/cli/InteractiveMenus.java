package org.ebenlib.cli;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.ebenlib.book.BookHandler;
import org.ebenlib.borrow.BorrowHandler;
import org.ebenlib.borrow.BorrowSettings;
import org.ebenlib.profile.ProfileHandler;
import org.ebenlib.report.ReportHandler;
import org.ebenlib.system.SystemHandler;
import org.ebenlib.user.UserHandler;

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
            menu.put("List Users",     () -> runWithPause(UserHandler::handleList));
            menu.put("Promote User",   () -> runWithPause(UserHandler::interactivePromote));
            menu.put("Demote User",    () -> runWithPause(UserHandler::interactiveDemote));
            menu.put("Activate User",  () -> runWithPause(() -> UserHandler.interactiveActivation(true)));
            menu.put("Suspend User",   () -> runWithPause(() -> UserHandler.interactiveActivation(false)));
            menu.put("Delete User",    () -> runWithPause(UserHandler::interactiveDelete));
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
        menu.put("Pay Borrow Fine", () -> runWithPause(() -> BorrowHandler.payFine(AuthHandler.requireActiveUser().getUsername())));
        menu.put("My Borrow History",() -> runWithPause( () -> BorrowHandler.handleHistory(Map.of(), false)));
        menu.put("Back",           () -> {});
        menu.put("Exit",           () -> System.exit(0));
        return menu;
    }

    public static Map<String, Runnable> getProfileMenu(String role) {
        Map<String, Runnable> menu = new LinkedHashMap<>();
        menu.put("View Profile", () -> runWithPause(() -> {
            var current = AuthHandler.getCurrentUser();
            if (current != null) ProfileHandler.handle(new String[]{"profile", "view"}, Map.of());
        }));

        menu.put("Update Profile", () -> runWithPause(() -> {
            var current = AuthHandler.getCurrentUser();
            if (current != null) ProfileHandler.handle(new String[]{"profile", "update"}, Map.of());
        }));

        menu.put("Change Password", () -> runWithPause(() -> {
            var current = AuthHandler.getCurrentUser();
            if (current != null) ProfileHandler.handle(new String[]{"profile", "password"}, Map.of());
        }));
        menu.put("Back",            () -> {});
        menu.put("Exit",            () -> System.exit(0));
        return menu;
    }

    public static Map<String, Runnable> getSystemMenu(String role) {
        Map<String, Runnable> menu = new LinkedHashMap<>();
        if (role.equalsIgnoreCase("Librarian")) {
            menu.put("Seed Demo Data", () -> runWithPause(SystemHandler::interactiveSeed));
            menu.put("Import Data from Folder", () -> runWithPause(SystemHandler::interactiveImport));
            menu.put("Export Data to Folder", () -> runWithPause(SystemHandler::interactiveExport));
            menu.put("Configure Settings", () -> runWithPause(SystemHandler::interactiveConfig));
            menu.put("View Settings", () -> runWithPause(BorrowSettings::printSettings));
        }
        menu.put("Back", () -> {});
        menu.put("Exit", () -> System.exit(0));
        return menu;
    }

    public static Map<String, Runnable> getReportMenu(String role) {
        Map<String,Runnable> m = new LinkedHashMap<>();
        m.put("Full Summary", () -> runWithPause(ReportHandler::fullReport));
        m.put("User Stats",   () -> runWithPause(ReportHandler::usersReport));
        m.put("Book Stats",   () -> runWithPause(ReportHandler::booksReport));
        m.put("Borrow Stats", () -> runWithPause(ReportHandler::borrowsReport));
        m.put("Back",         () -> {});
        m.put("Exit",         () -> System.exit(0));
        return m;
    }

    // private static void stub(String action) {
        // ConsoleUI.info("[Stub] " + action);
        // Do not press any key here — `runWithPause()` handles it
    // }

    public static void runWithPause(Runnable action) {
        action.run();
        ConsoleUI.pressEnterToContinue();
    }
}
