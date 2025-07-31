package org.ebenlib.user;

import org.ebenlib.cli.ConsoleUI;
import org.ebenlib.ds.EbenLibList;
import org.ebenlib.ds.EbenLibComparator;
import org.ebenlib.searchsort.Searcher;
import org.ebenlib.searchsort.Sorter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;

/**
 * Persists users to CSV:
 *   username,role,active
 */
public class UserStore {
    private static final Path CSV = Paths.get("app","src","main","resources","users.csv");
    private final EbenLibList<User> users = new EbenLibList<>();

    /** Load all users from CSV into memory */
    public void load() {
        users.clear();
        if (!Files.exists(CSV)) return;
        try (BufferedReader r = Files.newBufferedReader(CSV)) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] f = line.trim().split(",",4);
                if (f.length < 3) continue;
                String username = f[0].trim();
                String password = f[1].trim();
                String role        = f[2].trim();
                boolean active   = Boolean.parseBoolean(f[3].trim());
                users.add(new User(username, password, role, active));
            }
        } catch (IOException e) {
            ConsoleUI.error("Failed to load users: " + e.getMessage());
        }
    }

    /** Write current users back to CSV */
    public void save() {
        try {
            Files.createDirectories(CSV.getParent());
            try (BufferedWriter w = Files.newBufferedWriter(CSV,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                for (User u : users) {
                    w.write(String.join(",",
                        u.getUsername(),
                        u.getPassword(),       // This was missing before!
                        u.getRole(),
                        Boolean.toString(u.isActive())
                    ));
                    w.newLine();
                }
            }
        } catch (IOException e) {
            ConsoleUI.error("Failed to save users: " + e.getMessage());
        }
    }

    /** Return a defensive copy of all users */
    public EbenLibList<User> listAll() {
        return new EbenLibList<>(users);
    }

    /** Find one by exact username (case‑insensitive) */
    public Optional<User> findByUsername(String username) {
        EbenLibList<User> list = new EbenLibList<>(users);
        EbenLibComparator<User> comparator = EbenLibComparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER);
        Sorter.mergeSort(list, comparator);
        User key = new User(username); // assumes constructor User(String username)
        int index = Searcher.binarySearch(list, key, comparator);
        return index >= 0 ? Optional.of(list.get(index)) : Optional.empty();
    }


    /** Remove by username */
    public boolean delete(String username) {
        return users.removeIf(u -> u.getUsername().equalsIgnoreCase(username));
    }

    /** Change role */
    public boolean changeRole(String username, String newRole) {
        Optional<User> opt = findByUsername(username);
        System.out.println(opt);
        if (opt.isEmpty()) return false;
        opt.get().setRole(newRole);
        return true;
    }

    /** Suspend or reactivate */
    public boolean setActive(String username, boolean active) {
        Optional<User> opt = findByUsername(username);
        if (opt.isEmpty()) return false;
        opt.get().setActive(active);
        return true;
    }

    // Update username
public static boolean rename(String oldName, String newName) {
    try {
        EbenLibList<String> lines = new EbenLibList<>();
        try (BufferedReader reader = Files.newBufferedReader(CSV)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        EbenLibList<String> updated = new EbenLibList<>();
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts[0].equals(oldName)) {
                parts[0] = newName;
                updated.add(String.join(",", parts));
            } else {
                updated.add(line);
            }
        }
        Files.write(CSV, updated);
        return true;
    } catch (IOException e) {
        return false;
    }
}

    // Update password
    public static boolean updatePassword(String username, String newPwd) {
        try {
            EbenLibList<String> lines = new EbenLibList<>();

            try (BufferedReader reader = Files.newBufferedReader(CSV)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            EbenLibList<String> updated = new EbenLibList<>();
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts[0].equals(username)) {
                    parts[1] = newPwd;
                }
                updated.add(String.join(",", parts));
            }
            Files.write(CSV, updated);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // Check password
    public static boolean verifyPassword(String username, String pwd) {
        try (BufferedReader r = Files.newBufferedReader(CSV)) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(username) && parts[1].equals(pwd)) {
                    return true;
                }
            }
        } catch (IOException ignored) {}
        return false;
    }

}
