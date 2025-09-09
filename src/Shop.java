import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import Exceptions.InsufficientFundsException;

// This class holds the management of the shop
class Shop implements Serializable {
    // Create a unique identifier for the serialized class
    @Serial
    private static final long serialVersionUID = 1L;
    // Declare shop id and name variables
    private final int id;
    private final String name;
    // Declare string to store the shops currency
    private final String shopCurrency;
    // Declare list with the goods the shop has
    private final List<Goods> allGoods;
    // Declare list for the cashiers as one shop can have multiple cashiers
    private final List<Cashier> cashiers;
    // Variables for the total income and total cost of the store that are used to create the stores total profit
    private double totalIncome;
    private double totalCosts;

    // Set the variables for the overcharge, discounts and days the goods are not expired for food and non-food goods
    private final double percentageOverchargeFood;
    private final double percentageOverchargeNonFood;
    private final double percentageDecreaseFood;
    private final double percentageDecreaseNonFood;
    private final int daysTillExpiryFood;
    private final int daysTillExpiryNonFood;

    // Create the name of the folder that the information of the shop file will be stored in
    private static final String SHOP_INFO_FOLDER = "Shop_Info";


    public Shop(int id, String name, String shopCurrency, double percentageOverchargeFood, double percentageOverchargeNonFood,
                double percentageDecreaseFood, double percentageDecreaseNonFood,
                int daysTillExpiryFood, int daysTillExpiryNonFood) {
        // Initialize the instance variables
        this.id = id;
        this.name = name;
        this.shopCurrency = shopCurrency;
        this.allGoods = new ArrayList<>();
        this.cashiers = new ArrayList<>();
        // Initialize the total income by getting it from the file
        this.totalIncome = readTotalValueFromFile();
        this.totalCosts = 0;

        // Initialize the instance variables
        this.percentageOverchargeFood = percentageOverchargeFood;
        this.percentageOverchargeNonFood = percentageOverchargeNonFood;
        this.percentageDecreaseFood = percentageDecreaseFood;
        this.percentageDecreaseNonFood = percentageDecreaseNonFood;
        this.daysTillExpiryFood = daysTillExpiryFood;
        this.daysTillExpiryNonFood = daysTillExpiryNonFood;
    }

    // Create setters for the overcharge/discount variables
    public double getPercentageOverchargeFood() { return percentageOverchargeFood; }
    public double getPercentageOverchargeNonFood() { return percentageOverchargeNonFood; }
    public double getPercentageDecreaseFood() { return percentageDecreaseFood; }
    public double getPercentageDecreaseNonFood() { return percentageDecreaseNonFood; }
    public int getDaysTillExpiryFood() { return daysTillExpiryFood; }
    public int getDaysTillExpiryNonFood() { return daysTillExpiryNonFood; }

    // Create getters for the variables
    public String getName() { return name; }
    public String getCurrency() {
        return this.shopCurrency;
    }

    // Add the item to the list of goods the shop has and the goods total buying price to the total costs of the shop
    public void addGoodsToList(Goods goods) {
        allGoods.add(goods);
        totalCosts += goods.getBuyingPrice() * goods.getQuantity();
    }

    // Add a new cashier to the list of cashiers the shop has and the cashiers salary to the total costs of the shop
    public void addCashierToList(Cashier cashier) {
        cashiers.add(cashier);
        totalCosts += cashier.getSalary();
    }

    // This method returns the receipt object
    public Receipt sellGoods(Cashier cashier, Map<Integer, Integer> items, Client client) {
        // Declare the variable that will store the receipt
        Receipt receipt = null;
        try {
            // Calculate the total price of the receipt first
            double totalPrice = 0.0;
            // Iterate through the items in the goods map
            for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
                // Search for an item in the list with the current id and throw exception if the item is not in the map
                Goods goods = allGoods.stream().filter(g -> g.getId() == entry.getKey()).findFirst().orElse(null);
                // Use IllegalArgumentException for the exception as it is expected for the goods to exist
                if (goods == null) throw new IllegalArgumentException("Goods not found");

                // Calculate the selling price of the goods using the specific percentages of the current shop
                double sellingPrice = goods.calcSellingPrice(this);
                // Calculate the Total price of the receipt by multiplying calculated selling price by the quantity of the goods
                totalPrice += sellingPrice * entry.getValue();
            }

            // Call method that checks if the client has enough money based on the receipts total price
            // and throws an exception if the money are not enough
            client.checkIsEnoughMoney(totalPrice);

            // Create the file path and an object pointing to the shop info file
            String fileName = SHOP_INFO_FOLDER + "/" + id + "_shop_info.txt";
            File file = new File(fileName);
            int currentReceiptNumber = 0;

            // Check if the file exists and read the current receipt number from the shop information file
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
                    String line;
                    // Read each file of the line until the line that starts with Receipt number is found
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("Receipt number: ")) {
                            // Get the number after the : and turn it int an integer
                            currentReceiptNumber = Integer.parseInt(line.split(":")[1].trim());
                        }
                    }
                }
            }

            // Create the new receipt with the next receipt number and pass the arguments:
            // Shop ID, Shop Name, Cashier object, the current extracted receipt number + 1, Shop object
            receipt = new Receipt(id, name, cashier, currentReceiptNumber + 1, this);

            // Iterate through the items in the goods map
            for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
                // Search for an item in the list with the current id and throw exception if the item is not in the map
                Goods goods = allGoods.stream().filter(g -> g.getId() == entry.getKey()).findFirst().orElse(null);
                // Use IllegalArgumentException for the exception as it is expected for the goods to exist
                if (goods == null) throw new IllegalArgumentException("Goods not found");
                // Calculate the selling price of the goods using the specific percentages of the current shop
                double sellingPrice = goods.calcSellingPrice(this);
                // Add the sold goods to the receipt and the calculated selling price
                receipt.addSaleOfGoods(goods, entry.getValue(), sellingPrice);
            }

            // Call method from the receipt class that saves the receipts details to a txt format file
            receipt.saveReceiptToFile();

            // Call method from the receipt class that serializes and save the receipt as .ser file
            receipt.serializeReceiptObj();

            // Increment the receipt number and call method to update the total value in the shop info file
            currentReceiptNumber++;
            receipt.updateShopTotalValue(currentReceiptNumber);

            // Update the total income from the file after updating the receipt
            totalIncome = readTotalValueFromFile();

            // Throw an insufficient funds custom exception in case the client doesn't have enough money
        } catch (InsufficientFundsException e) {
            System.err.println("Insufficient funds error: " + e.getMessage());
            // Catch any other exceptions and print a generic message
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        return receipt;
    }

    private double readTotalValueFromFile() {
        // Create the file path and an object pointing to the shop info file
        String fileName = SHOP_INFO_FOLDER + "/" + id + "_shop_info.txt";
        double fileTotalIncome = 0.0;
        File file = new File(fileName);

        // Check if the file exists and read the current total value from the shop information file
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Read each file of the line until the line that starts with Total value:  is found
                    if (line.startsWith("Total value: ")) {
                        // Trim the line until only the total value is left and turn it into a double
                        String totalValueStr = line.split(":")[1].trim().replace(" BGN", "").trim();
                        fileTotalIncome = Double.parseDouble(totalValueStr);
                    }
                }
                // Throw an exception in case there was a problem during the file reading
            } catch (IOException e) {
                System.err.println("Error couldn't read the total income in the file " + e.getMessage());
            }
        }

        return fileTotalIncome;
    }

    // This method calculates and returns the total profit of the shop
    public double calculateProfit() {
        return totalIncome - totalCosts;
    }
}