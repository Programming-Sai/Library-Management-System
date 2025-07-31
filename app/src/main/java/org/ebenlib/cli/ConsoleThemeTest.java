package org.ebenlib.cli;


import org.ebenlib.ds.EbenLibList;

public class ConsoleThemeTest {
    public static void main(String[] args) {
        // ── Color Theme Test ─────────────────────────────────────────────
        ConsoleUI.header("🎨 ConsoleTheme Color Test");

        ConsoleUI.info("This is an info message");
        ConsoleUI.success("This is a success message");
        ConsoleUI.warning("This is a warning message");
        ConsoleUI.error("This is an error message");
        ConsoleUI.test();

        System.out.println("\n\n");

        // ── Table Printing Test ──────────────────────────────────────────
        ConsoleUI.header("📚 Paginated Table Test");

        String[] headers = {"ID", "Title", "Author"};
        int[] colWidths = {10, 50, 30};
        TablePrinter.printHeader(headers, colWidths);

        EbenLibList<String[]> books = new EbenLibList<>();
        for (int i = 1; i <= 25; i++) {
            books.add(new String[]{
                "B" + i,
                i % 2 == 0
                    ? i + " This is a Long text that should be wrapping any time soon. If it is not, then we have an issue at our hands."
                    : "Book Title " + i,
                "Author " + i
            });
        }

        TablePrinter.printTable(books, 10, colWidths);

        System.out.println();

        // ── Prompt / Confirm Test ────────────────────────────────────────
        ConsoleUI.header("🔠 Input Handling");

        String name = ConsoleUI.prompt("Enter your name:");
        if (ConsoleUI.confirm("Are you sure you entered your name correctly?")) {
            ConsoleUI.success("Welcome, " + name + "!");
        } else {
            ConsoleUI.warning("Name entry cancelled.");
        }

        System.out.println();

        // ── Spinner Test #1: With Done Message ───────────────────────────
        ConsoleUI.header("🔄 Spinner Test (with done message)");
        ConsoleUI.spin("Loading books from database...", () -> {
            try {
                Thread.sleep(3000); // Simulate work
            } catch (InterruptedException ignored) {}
        }, ConsoleUI.BRIGHT_GREEN, "Books Loaded Successfully");

        // ── Spinner Test #2: Without Done Message ────────────────────────
        ConsoleUI.header("🔄 Spinner Test (no done message)");
        ConsoleUI.spin("Processing data...", () -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {}
        });

        // ── All Done ─────────────────────────────────────────────────────
        ConsoleUI.success("✅ All tests completed.");
    }
}
