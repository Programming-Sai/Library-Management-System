package org.ebenlib.cli;

import org.ebenlib.ds.EbenLibList;

import java.util.Scanner;

public class TablePrinter {
    private static final Scanner scanner = new Scanner(System.in);
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int COLUMN_WIDTH = 20;

    // Split a string into multiple lines that fit the column width
    private static EbenLibList<String> wrapText(String text, int width) {
        EbenLibList<String> lines = new EbenLibList<>();
        int length = text.length();
        for (int i = 0; i < length; i += width) {
            int end = Math.min(i + width, length);
            lines.add(text.substring(i, end));
        }
        return lines;
    }

    // Format a row into wrapped lines with correct column widths
    private static EbenLibList<String> formatRow(String[] row, String rowColor, int[] colWidths) {
        EbenLibList<EbenLibList<String>> wrappedCells = new EbenLibList<>();
        int maxLines = 0;

        for (int i = 0; i < row.length; i++) {
            int width = i < colWidths.length ? colWidths[i] : COLUMN_WIDTH;
            EbenLibList<String> lines = wrapText(row[i] == null ? "" : row[i], width);
            wrappedCells.add(lines);
            maxLines = Math.max(maxLines, lines.size());
        }

        EbenLibList<String> formattedLines = new EbenLibList<>();

        for (int lineIndex = 0; lineIndex < maxLines; lineIndex++) {
            StringBuilder line = new StringBuilder("|");
            for (int colIndex = 0; colIndex < wrappedCells.size(); colIndex++) {
                int width = colIndex < colWidths.length ? colWidths[colIndex] : COLUMN_WIDTH;
                EbenLibList<String> lines = wrappedCells.get(colIndex);
                String cellLine = (lineIndex < lines.size()) ? lines.get(lineIndex) : "";
                line.append(" ")
                    .append(rowColor)
                    .append(String.format("%-" + width + "s", cellLine))
                    .append(ConsoleUI.RESET)
                    .append(" |");
            }
            formattedLines.add(line.toString());
        }

        return formattedLines;
    }


    public static void printHeader(String[] headers, int[] colWidths) {
        StringBuilder row = new StringBuilder("|");

        for (int i = 0; i < headers.length; i++) {
            int width = colWidths[i];
            row.append(" ")
               .append(ConsoleUI.BOLD)
               .append(String.format("%-" + width + "s", headers[i]))
               .append(ConsoleUI.RESET)
               .append(" |");
        }
        System.out.println(row.toString());

        String divider = "+";
        for (int i = 0; i < headers.length; i++) {
            divider += "-".repeat(colWidths[i] + 2) + "+";
        }
        System.out.println(divider);
    }

        public static void printHeader(String[] headers) {
            int[] widths = new int[headers.length];
            for (int i = 0; i < headers.length; i ++){
                widths[i] = COLUMN_WIDTH;
            }
            printHeader(headers, widths);
        }

    public static void printTable(EbenLibList<String[]> rows, int pageSize, int[] colWidths) {
        int total = rows.size();
        int current = 0;

        while (current < total) {
            int end = Math.min(current + pageSize, total);

            for (int i = current; i < end; i++) {
                String[] row = rows.get(i);
                String rowColor = (i % 2 == 0) ? ConsoleUI.WHITE : ConsoleUI.BG_BRIGHT_BLACK + ConsoleUI.WHITE;
                EbenLibList<String> formattedLines = formatRow(row, rowColor, colWidths);
                for (String line : formattedLines) {
                    System.out.println(line);
                }
            }

            current = end;

            if (current < total) {
                System.out.print(ConsoleUI.YELLOW + "-- More (ENTER to continue, Q to quit) -- " + ConsoleUI.RESET);
                String input = scanner.nextLine().trim().toLowerCase();
                System.out.print("\u001B[1A"); // Move up
                System.out.print("\u001B[2K"); // Clear line
                if (input.equals("q")) break;
            }
        }
    }

    public static void printTable(EbenLibList<String[]> rows) {
        int[] widths = new int[rows.size()];
        for (int i = 0; i < rows.size(); i ++){
            widths[i] = COLUMN_WIDTH;
        }
        printTable(rows, DEFAULT_PAGE_SIZE, widths);
    }

    public static void printTable(EbenLibList<String[]> rows, int pageSize) {
        int[] widths = new int[rows.size()];
        for (int i = 0; i < rows.size(); i ++){
            widths[i] = COLUMN_WIDTH;
        }
        printTable(rows, DEFAULT_PAGE_SIZE, widths);
    }

    public static void printTable(String[] row, int pageSize, int[] colWidths) {
        printTable(row, pageSize, colWidths);
    }
}
