public class Borrower {
    String id;
    String name;
    double fineOwed;

    public Borrower(String id, String name, double fineOwed) {
        this.id = id;
        this.name = name;
        this.fineOwed = fineOwed;
    }

    public void addFine(double amount) {
        this.fineOwed += amount;
    }

    @Override
    public String toString() {
        return id + "," + name + "," + String.format("%.2f", fineOwed);
    }
}
