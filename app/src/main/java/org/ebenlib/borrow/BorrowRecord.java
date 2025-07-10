package org.ebenlib.borrow;

import java.time.LocalDate;

public class BorrowRecord {
    int id;                 // default/package-private so BorrowStore can update
    String user;
    String bookId;
    LocalDate date;
    Status status;

    public BorrowRecord(int id, String user, String bookId, LocalDate date, Status status) {
        this.id = id;
        this.user = user;
        this.bookId = bookId;
        this.date = date;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public String getBookId() {
        return bookId;
    }

    public LocalDate getDate() {
        return date;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "BorrowRecord{" +
            "id=" + id +
            ", user='" + user + '\'' +
            ", bookId='" + bookId + '\'' +
            ", date=" + date +
            ", status=" + status +
            '}';
    }
}


enum Status { PENDING, APPROVED, REJECTED, RETURNED }
