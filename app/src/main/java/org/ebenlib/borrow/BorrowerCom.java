package org.ebenlib.borrow;


import java.util.ArrayList;
import java.util.List;



/**
 * The public class must match the filename (BorrowerCom.java).
 * It contains the main() method demonstrating the registry.
 */
public class BorrowerCom {
    public static void main(String[] args) {
        // Create a new registry instance (avoid static context errors)3.
        BorrowerRegistry registry = new BorrowerRegistry();

        // Create some Borrower objects
        Borrower borrower1 = new Borrower(1, "Alice Smith", "alice@example.com");
        Borrower borrower2 = new Borrower(2, "Bob Johnson", "bob@example.com");
        Borrower borrower3 = new Borrower(3, "Carol Williams", "carol@example.com");

        // Add borrowers to the registry
        registry.addBorrower(borrower1);
        registry.addBorrower(borrower2);
        registry.addBorrower(borrower3);

        // List all borrowers
        System.out.println("All borrowers in registry:");
        for (Borrower b : registry.listBorrowers()) {
            System.out.println(b);
        }

        // Get and print a specific borrower by ID
        System.out.println("\nGet borrower with ID 2:");
        Borrower fetched = registry.getBorrowerById(2);
        if (fetched != null) {
            System.out.println(fetched);
        }

        // Remove a borrower and show updated list
        System.out.println("\nRemoving borrower with ID 2.");
        registry.removeBorrowerById(2);

        System.out.println("Borrowers after removal:");
        for (Borrower b : registry.listBorrowers()) {
            System.out.println(b);
        }
    }
}

/**
 * Manages a collection of Borrower objects.
 * Uses an ArrayList to store borrowers.
 */
class BorrowerRegistry {
    private List<Borrower> borrowers = new ArrayList<>();

    /** Adds a borrower to the registry. */
    public void addBorrower(Borrower borrower) {
        borrowers.add(borrower);
    }

    /** Removes the borrower with the given ID, if present. */
    public boolean removeBorrowerById(int id) {
        for (Borrower b : borrowers) {
            if (b.getId() == id) {
                borrowers.remove(b);
                return true;
            }
        }
        return false; // Not found
    }

    /** Returns the borrower with the given ID, or null if not found. */
    public Borrower getBorrowerById(int id) {
        for (Borrower b : borrowers) {
            if (b.getId() == id) {
                return b;
            }
        }
        return null;
    }

    /** Returns a list of all borrowers. */
    public List<Borrower> listBorrowers() {
        return new ArrayList<>(borrowers);
    }
}

/**
 * Represents a borrower with an ID, name, and email.
 */
class Borrower {
    private int id;
    private String name;
    private String email;

    public Borrower(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // Getter methods (no setters needed for this example)
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    /** Returns a string representation of the borrower. */
    @Override
    public String toString() {
        return "Borrower{id=" + id + ", name='" + name + "', email='" + email + "'}";
    }
}