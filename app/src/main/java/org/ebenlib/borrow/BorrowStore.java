package org.ebenlib.borrow;

import org.ebenlib.cli.ConsoleUI;
import org.ebenlib.ds.EbenLibComparator;
import org.ebenlib.ds.EbenLibList;
import org.ebenlib.ds.EbenLibMap;
import org.ebenlib.ds.EbenLibPriorityQueue;
import org.ebenlib.searchsort.Searcher;
import org.ebenlib.searchsort.Sorter;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BorrowStore {
    private final Path file;
    private final EbenLibList<BorrowRecord> cache = new EbenLibList<>();
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
        EbenLibComparator<BorrowRecord> byId = EbenLibComparator.comparing(BorrowRecord::getId);
        int index = Searcher.binarySearch(cache, new BorrowRecord(id), byId);
        if (index != -1) {
            cache.get(index).setStatus(to);
            return true;
        }
        return false;
    }


    public BorrowRecord findById(int id) {
        EbenLibList<BorrowRecord> records = new EbenLibList<>(cache);
        EbenLibComparator<BorrowRecord> comparator = EbenLibComparator.comparing(BorrowRecord::getId);
        Sorter.mergeSort(records, comparator);
        BorrowRecord key = new BorrowRecord(id); // assumes constructor BorrowRecord(int id)
        int index = Searcher.binarySearch(records, key, comparator);
        return index >= 0 ? records.get(index) : null;
    }

    public EbenLibList<BorrowRecord> listByStatus(Status st) {
        return cache.filter(r -> r.getStatus() == st);
    }

    public EbenLibList<BorrowRecord> listAll() {
        return new EbenLibList<>(cache);
    }

    public EbenLibList<BorrowRecord> listByUser(String user) {
        return cache.filter(r -> r.getUser().equalsIgnoreCase(user));
    }

    /** Builds a PQ of overdue records (earliest approval first) */
    public EbenLibPriorityQueue<BorrowRecord> getOverdueQueue(int overdueDays) {
        LocalDate threshold = LocalDate.now().minusDays(overdueDays);
        EbenLibComparator<BorrowRecord> comp =
            EbenLibComparator.comparing(BorrowRecord::getDecisionDate);

        EbenLibPriorityQueue<BorrowRecord> pq = new EbenLibPriorityQueue<>(comp);

        for (BorrowRecord r : cache) {
            if (r.getStatus() == Status.APPROVED &&
                r.getDecisionDate() != null &&
                r.getDecisionDate().isBefore(threshold)) {
                pq.offer(r);
            }
        }
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
    
    public EbenLibMap<String, Long> countBorrowsByUser() {
        EbenLibMap<String, Long> counts = EbenLibMap.empty();
        for (BorrowRecord r : cache) {
            if (r.getStatus() == Status.APPROVED || r.getStatus() == Status.RETURNED) {
                String user = r.getUser();
                Long previous = counts.get(user);
                if (previous == null) previous = 0L;
                counts.put(user, previous + 1L);
            }
        }
        return counts;
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

    public double calculateFine(String username) {
        return listByUser(username).stream().mapToDouble(BorrowRecord::getFineOwed).sum();
    }

    public void clearFine(String username) {
        listByUser(username).forEach(r -> r.setFineOwed(0.0));
        save();
    }

    public void reduceFine(String username, double amount) {
        for (BorrowRecord r : listByUser(username)) {
            double f = r.getFineOwed();
            if (f > 0) {
                double deduction = Math.min(f, amount);
                r.setFineOwed(f - deduction);
                amount -= deduction;
                if (amount <= 0) break;
            }
        }
        save();
    }

    public void updateApproveDate(String username, LocalDate date) {
        for (BorrowRecord r : listByUser(username)) {
            r.setApproveDate(date); 
            r.recalculateFine();   
            if (r.getFineOwed() > 0) {
                r.setApproveDate(date);
            }
        }
        save();
    }

}
