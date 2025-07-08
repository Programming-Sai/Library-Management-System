package org.ebenlib.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TablePrinter {
    private static final Scanner scanner = new Scanner(System.in);
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int COLUMN_WIDTH = 20;

    // Split a string into multiple lines that fit the column width
    private static List<String> wrapText(String text, int width) {
        List<String> lines = new ArrayList<>();
        int length = text.length();
        for (int i = 0; i < length; i += width) {
            int end = Math.min(i + width, length);
            lines.add(text.substring(i, end));
        }
        return lines;
    }

    // Format a row into multiple lines (wrapped per cell)
    private static List<String> formatRow(String[] row, String rowColor) {
        List<List<String>> wrappedCells = new ArrayList<>();
        int maxLines = 0;

        // Wrap each cell and store wrapped lines
        for (String cell : row) {
            List<String> lines = wrapText(cell == null ? "" : cell, COLUMN_WIDTH);
            wrappedCells.add(lines);
            maxLines = Math.max(maxLines, lines.size());
        }

        List<String> formattedLines = new ArrayList<>();

        // Compose each line for the row
        for (int i = 0; i < maxLines; i++) {
            StringBuilder line = new StringBuilder("|");
            for (List<String> cellLines : wrappedCells) {
                String cellLine = (i < cellLines.size()) ? cellLines.get(i) : "";
                line.append(" ")
                    .append(rowColor)
                    .append(String.format("%-" + COLUMN_WIDTH + "s", cellLine))
                    .append(ConsoleTheme.RESET)
                    .append(" |");
            }
            formattedLines.add(line.toString());
        }

        return formattedLines;
    }

    public static void printHeader(String[] headers) {
        StringBuilder row = new StringBuilder("|");
        for (String header : headers) {
            row.append(" ")
               .append(ConsoleTheme.BOLD)
               .append(String.format("%-" + COLUMN_WIDTH + "s", header))
               .append(ConsoleTheme.RESET)
               .append(" |");
        }
        System.out.println(row.toString());

        String divider = "+";
        for (int i = 0; i < headers.length; i++) {
            divider += "-".repeat(COLUMN_WIDTH + 2) + "+";
        }
        System.out.println(divider);
    }

    public static void printTable(List<String[]> rows, int pageSize) {
        int total = rows.size();
        int current = 0;

        while (current < total) {
            int end = Math.min(current + pageSize, total);

            for (int i = current; i < end; i++) {
                String[] row = rows.get(i);
                String rowColor = (i % 2 == 0) ? ConsoleTheme.WHITE : ConsoleTheme.BG_BRIGHT_BLACK + ConsoleTheme.WHITE;
                List<String> formattedLines = formatRow(row, rowColor);
                for (String line : formattedLines) {
                    System.out.println(line);
                }
            }

            current = end;

            if (current < total) {
                System.out.print(ConsoleTheme.YELLOW + "-- More (ENTER to continue, Q to quit) -- " + ConsoleTheme.RESET);
                String input = scanner.nextLine().trim().toLowerCase();
                System.out.print("\u001B[1A"); // Move up
                System.out.print("\u001B[2K"); // Clear line
                if (input.equals("q")) break;
            }
        }
    }

    public static void printTable(List<String[]> rows) {
        printTable(rows, DEFAULT_PAGE_SIZE);
    }
}
