package org.ebenlib.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FileUtil {

    /**
     * Read every line of the CSV at `path`, parse with `mapper`, return the list.
     */
    public static <T> List<T> readCSV(Path path, Function<String, T> mapper) {
        try {
            if (Files.notExists(path)) return new ArrayList<>();
            return Files.readAllLines(path, StandardCharsets.UTF_8).stream()
                    .filter(line -> !line.isBlank())
                    .map(mapper)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV " + path + ": " + e.getMessage(), e);
        }
    }

    /**
     * Write `items` to CSV at `path`, one line per item via `toCsv`.
     * Overwrites existing file.
     */
    public static <T> void writeCSV(Path path, List<T> items, Function<T, String> toCsv) {
        try {
            List<String> lines = items.stream()
                                      .map(toCsv)
                                      .collect(Collectors.toList());
            Files.write(path, lines, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write CSV " + path + ": " + e.getMessage(), e);
        }
    }
}
