package org.ebenlib.borrow;

import org.ebenlib.cli.ConsoleUI;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class BorrowStore {
    private final Path file;
    private final List<BorrowRecord> cache = new ArrayList<>();
    private int nextId = 1;

    public BorrowStore(Path file) {
        this.file = file;
    }

    public void load() {
        cache.clear();
        if (!Files.exists(file)) return;
        try (BufferedReader r = Files.newBufferedReader(file)) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 8) continue; // new format
                BorrowRecord rec = new BorrowRecord(
                    Integer.parseInt(p[0].trim()),
                    p[1].trim(),
                    p[2].trim(),
                    LocalDate.parse(p[3].trim()),
                    Status.valueOf(p[4].trim())
                );
                // stamp optional fields
                rec.decisionDate = p[5].trim().isEmpty() ? null : LocalDate.parse(p[5].trim());
                rec.returnDate   = p[6].trim().isEmpty() ? null : LocalDate.parse(p[6].trim());
                rec.fineOwed     = Double.parseDouble(p[7].trim());
                cache.add(rec);
                nextId = Math.max(nextId, rec.getId() + 1);
            }
            // ensure fines are up to date
            cache.forEach(BorrowRecord::recalculateFine);
        } catch (IOException e) {
            ConsoleUI.warning("Failed to load borrow store: " + e.getMessage());
        }
    }

    public void save() {
        try {
            Files.createDirectories(file.getParent());
            try (BufferedWriter w = Files.newBufferedWriter(file,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                for (var r : cache) {
                    w.write(String.join(",",
                        String.valueOf(r.getId()),
                        r.getUser(),
                        r.getBookId(),
                        r.getRequestDate().toString(),
                        r.getStatus().name(),
                        r.getDecisionDate() == null ? "" : r.getDecisionDate().toString(),
                        r.getReturnDate()   == null ? "" : r.getReturnDate().toString(),
                        String.valueOf(r.getFineOwed())
                    ));
                    w.newLine();
                }
            }
        } catch (IOException e) {
            ConsoleUI.error("Failed to save borrow store: " + e.getMessage());
        }
    }

    public int addRequest(String user, String bookId) {
        BorrowRecord rec = new BorrowRecord(nextId++, user, bookId, LocalDate.now(), Status.PENDING);
        cache.add(rec);
        return rec.getId();
    }

    public boolean updateStatus(int id, Status to) {
        for (BorrowRecord r : cache) {
            if (r.getId() == id) {
                r.setStatus(to);
                return true;
            }
        }
        return false;
    }

    public BorrowRecord findById(int id) {
        return cache.stream().filter(r -> r.getId() == id).findFirst().orElse(null);
    }

    public List<BorrowRecord> listByStatus(Status st) {
        return cache.stream()
            .filter(r -> r.getStatus() == st)
            .collect(Collectors.toList());
    }

    public List<BorrowRecord> listAll() {
        return new ArrayList<>(cache);
    }

    public List<BorrowRecord> listByUser(String user) {
        return cache.stream()
            .filter(r -> r.getUser().equalsIgnoreCase(user))
            .collect(Collectors.toList());
    }

    /** Builds a PQ of overdue records (earliest approval first) */
    public PriorityQueue<BorrowRecord> getOverdueQueue(int overdueDays) {
        LocalDate threshold = LocalDate.now().minusDays(overdueDays);
        PriorityQueue<BorrowRecord> pq = new PriorityQueue<>(Comparator.comparing(r -> r.getDecisionDate()));
        cache.stream()
            .filter(r -> r.getStatus() == Status.APPROVED
                      && r.getDecisionDate() != null
                      && r.getDecisionDate().isBefore(threshold))
            .forEach(pq::add);
        return pq;
    }

    /** Total fine across all APPROVED (not yet returned) records for this user */
    public double getTotalFineForUser(String username) {
        return cache.stream()
            .filter(r -> r.getUser().equalsIgnoreCase(username)
                      && r.getStatus() == Status.APPROVED)
            .mapToDouble(BorrowRecord::getFineOwed)
            .sum();
    }
    
    public Map<String, Long> countBorrowsByUser() {
        return cache.stream()
            .filter(r -> r.getStatus() == Status.APPROVED || r.getStatus() == Status.RETURNED)
            .collect(Collectors.groupingBy(BorrowRecord::getUser, Collectors.counting()));
    }

    public long countByBook(String bookId) {
        return cache.stream()
            .filter(r -> r.getBookId().equals(bookId))
            .count();
    }

    public long countReturnedByBook(String bookId) {
        return cache.stream()
            .filter(r -> r.getBookId().equals(bookId) && r.getStatus() == Status.RETURNED)
            .count();
    }

    public void refreshAllFines() {
        LocalDate now = LocalDate.now();
        for (BorrowRecord r : cache) {
            if (r.getStatus() == Status.APPROVED && r.getDecisionDate() != null) {
                long daysOverdue = ChronoUnit.DAYS.between(
                    r.getDecisionDate().plusDays(BorrowSettings.loanPeriodDays),
                    now
                );
                r.fineOwed = daysOverdue > 0 ? daysOverdue * BorrowSettings.finePerDay : 0.0;
            }
        }
        save();
    }

    public void updateUsername(String oldUsername, String newUsername) {
        for (BorrowRecord r : cache) {
            if (r.getUser().equalsIgnoreCase(oldUsername)) {
                r.setUser(newUsername); // you’ll need to allow mutation or rebuild record
            }
        }
        save(); // persist the changes
    }

}
