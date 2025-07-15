Overdue & Fine Engine  
Part of the Book Lending & Cataloguing System for Ebenezer Community Library
Developer
**Name: NanaYawMinta  
Role: Overdue & Fine Engine Module

 Module Description

This module detects overdue book loans and calculates fines for borrowers who fail to return books within the expected time frame. It updates borrower records and generates a report of overdue books and outstanding fines.

The system is:
- Offline-first
- Console-based
- Implemented in Java
- Uses only data structures and file storage 

📂 Folder Structure

```

Overdue/
│
├── data/
│   ├── borrowers.txt           # Input file with borrower info
│   ├── transactions.txt        # Input file with lending transactions
│   └── overdue\_report.txt      # Output file with overdue report
│
├── src/
│   ├── Main.java               # Main launcher
│   ├── Borrower.java           # Model for borrower data
│   ├── Transaction.java        # Model for lending transactions
│   └── OverdueTracker.java     # Core overdue and fine engine
│
├── README.md                   # This documentation file
└── .gitignore                  # Optional – exclude compiled files

```
File Descriptions

 1. `borrowers.txt`
Stores borrower information in the format:

```

BorrowerID,Name,FineOwed

```

Example:
```

B001,Ama Serwaa,0.0
B002,Kwame Mensah,3.0

```


 2. `transactions.txt`
Stores lending records in the format:

```

ISBN,BorrowerID,BorrowDate,ReturnDate,IsReturned

```

Example:
```

ISBN001,B001,2025-06-10,2025-06-20,false

```


 3. `overdue_report.txt` (Output)
Generated automatically after overdue check. Contains list of overdue items and calculated fines.

Example Output:
```

Overdue Report – 2025-07-10

ISBN: ISBN001 | Borrower: Ama Serwaa | Days Overdue: 6 | Fine: GHS 6.00

````


 How It Works

1. Load borrower data from `borrowers.txt`
2. Load active (not returned) transactions from `transactions.txt`
3. For each transaction:
   - Check if return date + 14 days grace period has passed
   - If overdue, calculate fine: **GHS 1.00 per extra day**
   - Add fine to borrower
4. Save:
   - Updated borrower records to `borrowers.txt`
   - Report to `overdue_report.txt`


 How to Run

1. Open terminal in root project folder (`BookLendingSystem/`)
2. Compile Java files:
   ```bash
   Javac src/*.java
````

3. Run program:

   ```bash
   Java -cp src Main
   ```




System Features and Implementation
Overdue Prioritization

Implementation: PriorityQueue<Transaction>

Purpose: Process soonest due books first

Borrower Lookup

Implementation: HashMap<String, Borrower>

Purpose: Fast access and update of borrower fines

Date Handling

Implementation: LocalDate (Java Time API)

Purpose: Accurate date comparisons

File Storage

Implementation: .txt with BufferedReader/Writer

Purpose: Offline-first, portable, and easy to debug

 Files Used in the System
Borrowers.txt

Provided By: Borrower Registry Team

Purpose: Stores borrower records and fines

Transactions.txt

Provided By: Lending System Team

Purpose: Holds records of active book loans

