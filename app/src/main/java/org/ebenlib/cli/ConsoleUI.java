package org.ebenlib.cli;

import java.io.IOException;
import java.util.List;

public class ConsoleUI {

    
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
    private static final long SPINNER_INTERVAL_MS = 100;

    /**
     * Run a task with a default blue spinner and success message.
     */
    public static void spin(String message, Runnable task) {
        spin(message, task, BRIGHT_BLUE, null);
    }

    /**
     * Run a task while displaying a terminal spinner.
     *
     * @param message       The message to show while spinning
     * @param task          The task to run
     * @param spinnerColor  ANSI color for spinner character
     * @param doneMessage   Optional message to show when complete (null for none)
     */
    public static void spin(String message, Runnable task, String spinnerColor, String doneMessage) {
        final boolean[] running = { true };

        Thread spinnerThread = new Thread(() -> {
            int idx = 0;
            while (running[0]) {
                String frame = "\r" + spinnerColor + SPINNER_STEPS[idx % SPINNER_STEPS.length] + RESET + " " + message;
                System.out.print(frame);
                System.out.flush();
                idx++;
                try {
                    Thread.sleep(SPINNER_INTERVAL_MS);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        spinnerThread.start();

        try {
            task.run();
        } catch (Exception e) {
            running[0] = false;
            spinnerThread.interrupt();
            try {
                spinnerThread.join();
            } catch (InterruptedException ignored) {}

            // Clear line
            System.out.print("\r\u001B[2K");
            error("✖ " + message);
            return;
        }

        running[0] = false;
        spinnerThread.interrupt();

        try {
            spinnerThread.join();
        } catch (InterruptedException ignored) {}

        // Clear line
        System.out.print("\r\u001B[2K");

        if (doneMessage != null && !doneMessage.isEmpty()) {
            success("✔ " + doneMessage);
        } else if (doneMessage != null) {
            // If explicitly empty string is passed, print nothing
        } else {
            success("✔ " + message);
        }
    }


    @FunctionalInterface
    public interface Colorizer {
        String apply(String input, int lineIndex);
    }


    public static void printLogo(Colorizer colorizer) {
        String[] logo = {
            "                                Æ Æ Æ                                                                                                                        ",
            "                          ÆÆÆÆÆ       ÆÆÆÆÆ                                                                                                                  ",
            "                       ÆÆ                   ÆÆ                                                                                                               ",
            "                    ÆÆ                          Æ                                                                                                            ",
            "                  Æ                               Æ                                                                                                          ",
            "                ÆÆ                                 ÆÆ                                                                                                        ",
            "               ÆÆ       ÆÆÆÆ              ÆÆÆÆ       Æ                                                                                                       ",
            "              Æ         ÆÆ ÆÆÆÆ        ÆÆÆÆ ÆÆ        Æ                                                                                                      ",
            "             Æ      ÆÆ  ÆÆ    ÆÆÆ   ÆÆÆ    ÆÆÆ  Æ      Æ                                                                                                      ",
            "            Æ       ÆÆ  ÆÆ      ÆÆÆÆÆ      ÆÆÆ ÆÆ       Æ                                                                                                      ",
            "            Æ       ÆÆ  ÆÆ        ÆÆ       ÆÆÆ ÆÆ       Æ                                                                                                     ",
            "           ÆÆ       ÆÆ  ÆÆ        ÆÆ       ÆÆÆ ÆÆ       ÆÆ                                                                                                    ",
            "            Æ       ÆÆ  ÆÆ        ÆÆ       ÆÆÆ ÆÆ       Æ                                                                                                     ",
            "            Æ       ÆÆ  ÆÆ        ÆÆ       ÆÆÆ ÆÆ       Æ                                                                                                      ",
            "             Æ      ÆÆ  ÆÆ        ÆÆ       ÆÆÆ ÆÆ      Æ                                                                                                     ",
            "             ÆÆ     ÆÆ  ÆÆÆÆ      ÆÆ      ÆÆÆÆ ÆÆ     ÆÆ                                                                                                     ",
            "              ÆÆ    ÆÆ      ÆÆÆ   ÆÆ  ÆÆÆÆ     ÆÆ     Æ                                                                                                      ",
            "               Æ     ÆÆÆÆÆÆÆÆ  ÆÆ ÆÆÆÆÆ  ÆÆÆÆÆÆÆÆ    Æ                                                                                                       ",
            "                Æ            ÆÆÆÆÆÆÆÆÆÆÆÆ           Æ                                                                                                        ",
            "                 ÆÆ             ÆÆÆÆÆ             ÆÆ                                                                                                         ",
            "                   Æ                             Æ                                                                                                           ",
            "                    ÆÆ                         ÆÆ                                                                                                            ",
            "                       ÆÆ                 ÆÆÆÆÆÆÆÆÆÆÆÆÆ  ÆÆÆÆÆÆÆÆÆÆÆÆ    ÆÆÆÆÆÆÆÆÆÆÆÆ   ÆÆÆÆÆ      ÆÆÆÆ   ÆÆÆÆÆÆ         ÆÆÆÆÆÆÆ   ÆÆÆÆÆÆÆÆÆÆÆ                ",
            "                          ÆÆÆ           ÆÆ ÆÆÆÆÆ    ÆÆÆ   ÆÆÆÆ    ÆÆÆÆ    ÆÆÆÆÆ    ÆÆ    ÆÆÆÆÆ      ÆÆ     ÆÆÆÆÆ          ÆÆÆÆÆ     ÆÆÆÆ   ÆÆÆÆÆ              ",
            "                               ÆÆÆÆÆÆÆ     ÆÆÆÆÆ     Æ    ÆÆÆÆ    ÆÆÆÆÆ   ÆÆÆÆÆ     Æ    ÆÆÆÆÆÆ    ÆÆÆ     ÆÆÆÆÆ          ÆÆÆÆÆ     ÆÆÆÆ    ÆÆÆÆ              ",
            "                                           ÆÆÆÆÆ          ÆÆÆÆ    ÆÆÆÆÆ   ÆÆÆÆÆ          ÆÆÆÆÆÆÆ   ÆÆÆ     ÆÆÆÆÆ          ÆÆÆÆÆ     ÆÆÆÆ    ÆÆÆÆ              ",
            "                                           ÆÆÆÆÆ   ÆÆ     ÆÆÆÆ   ÆÆÆÆÆ    ÆÆÆÆÆ   ÆÆ     ÆÆ ÆÆÆÆÆ  ÆÆÆ     ÆÆÆÆÆ          ÆÆÆÆÆ     ÆÆÆÆ   ÆÆÆÆ               ",
            "                                           ÆÆÆÆÆÆÆÆÆÆ     ÆÆÆÆÆÆÆÆÆ       ÆÆÆÆÆÆÆÆÆÆ     ÆÆ  ÆÆÆÆÆ  ÆÆ     ÆÆÆÆÆ          ÆÆÆÆÆ     ÆÆÆÆÆÆÆÆÆ                ",
            "                                           ÆÆÆÆÆ  ÆÆÆ     ÆÆÆÆ   ÆÆÆÆÆ    ÆÆÆÆÆ  ÆÆÆ     ÆÆ   ÆÆÆÆÆÆÆÆ     ÆÆÆÆÆ          ÆÆÆÆÆ     ÆÆÆÆ   ÆÆÆÆÆ              ",
            "                                           ÆÆÆÆÆ          ÆÆÆÆ     ÆÆÆÆ   ÆÆÆÆÆ   Æ      ÆÆ    ÆÆÆÆÆÆÆ     ÆÆÆÆÆ          ÆÆÆÆÆ     ÆÆÆÆ    ÆÆÆÆÆ            ",
            "                                           ÆÆÆÆÆ          ÆÆÆÆ     ÆÆÆÆ   ÆÆÆÆÆ          ÆÆ     ÆÆÆÆÆÆ     ÆÆÆÆÆ          ÆÆÆÆÆ     ÆÆÆÆ    ÆÆÆÆÆ            ",
            "                                           ÆÆÆÆÆ     ÆÆ   ÆÆÆÆ     ÆÆÆÆ   ÆÆÆÆÆ     ÆÆ   ÆÆ      ÆÆÆÆÆ     ÆÆÆÆÆ     ÆÆ   ÆÆÆÆÆ     ÆÆÆÆ    ÆÆÆÆÆ            ",
            "                                           ÆÆÆÆÆ     ÆÆ   ÆÆÆÆ    ÆÆÆÆÆ   ÆÆÆÆÆ    ÆÆÆ   ÆÆ       ÆÆÆÆ     ÆÆÆÆ     ÆÆÆ   ÆÆÆÆÆ     ÆÆÆÆ   ÆÆÆÆÆ              ",
            "                                          ÆÆÆÆÆÆÆÆÆÆÆÆÆ ÆÆÆÆÆÆÆÆÆÆÆÆÆ    ÆÆÆÆÆÆÆÆÆÆÆÆ  ÆÆÆÆÆÆ      ÆÆÆ   ÆÆÆÆÆÆÆÆÆÆÆÆÆÆ  ÆÆÆÆÆÆÆ  ÆÆÆÆÆÆÆÆÆÆÆÆ                "
        };

        for (int i = 0; i < logo.length; i++) {
            System.out.println(colorizer.apply(logo[i], i));
        }
    }

    public static Colorizer gradientColorizer(List<String> colors, double angleDegrees) {
        return (line, lineIndex) -> {
            if (colors.isEmpty()) return line; // Fallback

            StringBuilder colored = new StringBuilder();

            // Convert angle to radians and calculate slant ratio
            double radians = Math.toRadians(angleDegrees);
            double slantFactor = Math.tan(radians); // How steep the slant is

            int maxColorIndex = colors.size();

            for (int charIndex = 0; charIndex < line.length(); charIndex++) {
                char ch = line.charAt(charIndex);

                // Compute gradient offset
                double gradientPos = (lineIndex + charIndex * slantFactor) / 10.0;

                // Clamp to valid color index
                int colorIndex = (int) Math.floor(gradientPos) % maxColorIndex;
                if (colorIndex < 0) colorIndex += maxColorIndex;

                String color = colors.get(colorIndex);
                colored.append(color).append(ch);
            }

            return colored.append(ConsoleUI.RESET).toString();
        };
    }

}
