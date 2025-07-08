package org.ebenlib.cli;

import java.util.ArrayList;
import java.util.List;

public class ConsoleThemeTest {
    public static void main(String[] args) {
        // Test color printing
        ConsoleTheme.header("🎨 ConsoleTheme Color Test");
        ConsoleTheme.info("This is an info message");
        ConsoleTheme.success("This is a success message");
        ConsoleTheme.warning("This is a warning message");
        ConsoleTheme.error("This is an error message");
        System.out.println("\n\n");
        ConsoleTheme.test();

        // Test table printing
        ConsoleTheme.header("📚 Paginated Table Test");

        String[] headers = {"ID", "Title", "Author"};
        TablePrinter.printHeader(headers);

        List<String[]> books = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            books.add(new String[]{
                "B" + i,
                i%2==1 ? ("Book Title " + i) : (i+" This is a Long text that should be wrapping any time soon. if it is not then we have an issue at our hands."),
                "Author " + i
            });
        }

        ConsoleTheme.spin("Loading books...", () -> {
        String name = ConsoleTheme.prompt("Enter your name:");
            if (ConsoleTheme.confirm("Are you sure?")) {
            System.out.println(name);
            }
        });

        TablePrinter.printTable(books, 10); // 5 rows per page
    }
}
