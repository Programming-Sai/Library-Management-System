package org.ebenlib.cli;

import java.io.IOException;

public class ConsoleTheme {

    
        // Reset color
    public static final String RESET = "\u001B[0m";

    // Regular colors
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    // Bright colors
    public static final String BRIGHT_BLACK = "\u001B[90m";
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_BLUE = "\u001B[94m";
    public static final String BRIGHT_PURPLE = "\u001B[95m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_WHITE = "\u001B[97m";

    // Background colors
    public static final String BG_BLACK = "\u001B[40m";
    public static final String BG_RED = "\u001B[41m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String BG_YELLOW = "\u001B[43m";
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_PURPLE = "\u001B[45m";
    public static final String BG_CYAN = "\u001B[46m";
    public static final String BG_WHITE = "\u001B[47m";

    // Bright background colors
    public static final String BG_BRIGHT_BLACK = "\u001B[100m";
    public static final String BG_BRIGHT_RED = "\u001B[101m";
    public static final String BG_BRIGHT_GREEN = "\u001B[102m";
    public static final String BG_BRIGHT_YELLOW = "\u001B[103m";
    public static final String BG_BRIGHT_BLUE = "\u001B[104m";
    public static final String BG_BRIGHT_PURPLE = "\u001B[105m";
    public static final String BG_BRIGHT_CYAN = "\u001B[106m";
    public static final String BG_BRIGHT_WHITE = "\u001B[107m";

    // Text attributes
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";
    public static final String ITALIC = "\u001B[3m";
    public static final String UNDERLINE = "\u001B[4m";
    public static final String BLINK = "\u001B[5m";
    public static final String REVERSE = "\u001B[7m";
    public static final String HIDDEN = "\u001B[8m";
    public static final String STRIKETHROUGH = "\u001B[9m";


    public static void test() {
        System.out.println("\n" + BOLD + "== Console Color Test ==" + RESET);

        // Standard colors
        println("BLACK", BLACK);
        println("RED", RED);
        println("GREEN", GREEN);
        println("YELLOW", YELLOW);
        println("BLUE", BLUE);
        println("PURPLE", PURPLE);
        println("CYAN", CYAN);
        println("WHITE", WHITE);

        // Bright colors
        println("BRIGHT BLACK", BRIGHT_BLACK);
        println("BRIGHT RED", BRIGHT_RED);
        println("BRIGHT GREEN", BRIGHT_GREEN);
        println("BRIGHT YELLOW", BRIGHT_YELLOW);
        println("BRIGHT BLUE", BRIGHT_BLUE);
        println("BRIGHT PURPLE", BRIGHT_PURPLE);
        println("BRIGHT CYAN", BRIGHT_CYAN);
        println("BRIGHT WHITE", BRIGHT_WHITE);

        // Background colors
        System.out.println("\n" + BOLD + "== Background Colors ==" + RESET);
        println("BG BLACK", BG_BLACK + WHITE);
        println("BG RED", BG_RED + WHITE);
        println("BG GREEN", BG_GREEN + WHITE);
        println("BG YELLOW", BG_YELLOW + BLACK);
        println("BG BLUE", BG_BLUE + WHITE);
        println("BG PURPLE", BG_PURPLE + WHITE);
        println("BG CYAN", BG_CYAN + BLACK);
        println("BG WHITE", BG_WHITE + BLACK);

        // Bright background colors
        println("BG BRIGHT BLACK", BG_BRIGHT_BLACK + WHITE);
        println("BG BRIGHT RED", BG_BRIGHT_RED + BLACK);
        println("BG BRIGHT GREEN", BG_BRIGHT_GREEN + BLACK);
        println("BG BRIGHT YELLOW", BG_BRIGHT_YELLOW + BLACK);
        println("BG BRIGHT BLUE", BG_BRIGHT_BLUE + BLACK);
        println("BG BRIGHT PURPLE", BG_BRIGHT_PURPLE + BLACK);
        println("BG BRIGHT CYAN", BG_BRIGHT_CYAN + BLACK);
        println("BG BRIGHT WHITE", BG_BRIGHT_WHITE + BLACK);

        // Text attributes
        System.out.println("\n" + BOLD + "== Text Attributes ==" + RESET);
        println("BOLD TEXT", BOLD + WHITE);
        println("DIM TEXT", DIM + WHITE);
        println("ITALIC TEXT", ITALIC + WHITE);
        println("UNDERLINED TEXT", UNDERLINE + WHITE);
        println("BLINKING TEXT", BLINK + WHITE);
        println("REVERSED TEXT", REVERSE + WHITE);
        println("HIDDEN TEXT", HIDDEN + WHITE);
        println("STRIKETHROUGH TEXT", STRIKETHROUGH + WHITE);

        System.out.println("\n\n");

        for (int i = 30; i <= 37; i++) {
            System.out.println("\u001B[" + i + "mColor " + i + " (Regular)\u001B[0m");
        }
        for (int i = 90; i <= 97; i++) {
            System.out.println("\u001B[" + i + "mColor " + i + " (Bright)\u001B[0m");
        }

    }


    // Wrappers
    public static void print(String text, String color) {
        System.out.print(color + text + RESET);
    }

    public static void println(String text, String color) {
        System.out.println(color + text + RESET);
    }

    public static void info(String message) {
        println("ℹ️  " + message, BLUE);
    }

    public static void success(String message) {
        println("✅ " + message, GREEN);
    }

    public static void warning(String message) {
        println("⚠️  " + message, YELLOW);
    }

    public static void error(String message) {
        println("❌ " + message, RED);
    }

    public static void header(String title) {
        println("\n" + BOLD + title + RESET, PURPLE);
    }

    public static String prompt(String message) {
        System.out.print(BOLD + message + RESET + " ");
        try {
            byte[] buf = new byte[1024];
            int len = System.in.read(buf);
            if (len <= 0) return "";
            return new String(buf, 0, len).trim();
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * Prompts for yes/no (returns true on 'y' or 'yes', false otherwise).
     */
    public static boolean confirm(String message) {
        String ans = prompt(message + " [y/N]:").toLowerCase();
        return ans.equals("y") || ans.equals("yes");
    }

    // ── Loading Spinner ─────────────────────────────────────────────────────────
    private static final String[] SPINNER_STEPS = { "|", "/", "-", "\\" };

    /**
     * Displays a spinner until the provided Runnable completes.
     */
    public static void spin(String message, Runnable task) {
        Thread spinner = new Thread(() -> {
            int idx = 0;
            while (!Thread.currentThread().isInterrupted()) {
                System.out.print("\r" + BRIGHT_BLUE + SPINNER_STEPS[idx % SPINNER_STEPS.length]
                    + RESET + " " + message);
                idx++;
                try { Thread.sleep(200); } catch (InterruptedException e) { break; }
            }
        });
        spinner.start();
        try {
            task.run();
        } finally {
            spinner.interrupt();
            System.out.print("\r"); // clear line
        }
    }
}
