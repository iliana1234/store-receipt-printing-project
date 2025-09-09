import java.io.Serial;
import java.io.Serializable;

// Class that holds the management for the cashier
class Cashier implements Serializable {
    // Create a unique identifier for the serialized class
    @Serial
    private static final long serialVersionUID = 1L;
    // Declare the Cashier ID
    int id;
    // Declare the Cashier Names
    private final String names;
    // Declare the Cashier's salary
    private final double salary;

    public Cashier(int id, String names, double salary) {
        // Initialize the instance variables as the passed parameters values
        this.id = id;
        this.names = names;
        this.salary = salary;
    }

    public int getId() { return id; }
    public String getName() { return names; }
    public double getSalary() { return salary; }
}