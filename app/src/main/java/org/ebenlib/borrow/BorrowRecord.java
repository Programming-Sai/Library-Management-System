package org.ebenlib.borrow;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BorrowRecord {
    int id;                 // package‑private so BorrowStore can update
    String user;
    String bookId;
    LocalDate requestDate;      // when the request was made
    LocalDate decisionDate;     // when it was APPROVED or REJECTED
    LocalDate returnDate;       // when it was returned
    Status status;
    double fineOwed = 0.0;

    public BorrowRecord(int id, String user, String bookId, LocalDate requestDate, Status status) {
        this.id = id;
        this.user = user;
        this.bookId = bookId;
        this.requestDate = requestDate;
        this.status = status;
    }

    // getters...

    public int getId()            { return id; }
    public String getUser()       { return user; }
    public String getBookId()     { return bookId; }
    public LocalDate getRequestDate() { return requestDate; }
    public LocalDate getDecisionDate() { return decisionDate; }
    public LocalDate getReturnDate()   { return returnDate; }
    public Status getStatus()     { return status; }
    public double getFineOwed()   { return fineOwed; }

    public void setUser(String newUser){
        user = newUser;
    }

    /** Update status and stamp decisionDate / returnDate as needed, then recalc fine */
    public void setStatus(Status newStatus) {
        this.status = newStatus;
        LocalDate today = LocalDate.now();
        if ((newStatus == Status.APPROVED || newStatus == Status.REJECTED) && decisionDate == null) {
            this.decisionDate = today;
        }
        if (newStatus == Status.RETURNED) {
            this.returnDate = today;
        }
        recalculateFine();
    }

    /** Compute fine only if APPROVED and overdue */
    public void recalculateFine() {
        if (status != Status.APPROVED || decisionDate == null) {
            fineOwed = 0.0;
            return;
        }
        long daysSinceApproval = ChronoUnit.DAYS.between(decisionDate, LocalDate.now());
        long overdue = Math.max(0, daysSinceApproval - BorrowSettings.loanPeriodDays);
        fineOwed = overdue * BorrowSettings.finePerDay;
    }

    @Override
    public String toString() {
        return String.format(
            "BorrowRecord{id=%d, user='%s', bookId='%s', requested=%s, status=%s, fine=%.2f}",
            id, user, bookId, requestDate, status, fineOwed
        );
    }

    public void setFineOwed(double d) {
        this.fineOwed = d;
    }

    public void setApproveDate(LocalDate date) {
        if (this.status == Status.APPROVED){
            this.decisionDate = date;
        }
    }
}

enum Status { PENDING, APPROVED, REJECTED, RETURNED }
