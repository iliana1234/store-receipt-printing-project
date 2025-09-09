import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import Exceptions.InsufficientQuantityException;

// This class holds the management for the receipt creation
class Receipt implements Serializable {
    // Create a unique identifier for the serialized class
    @Serial
    private static final long serialVersionUID = 1L;

    private final int receiptId;
    // The cashiers object
    private final Cashier cashier;
    private final LocalDateTime dateTime;
    // Map with the goods object and sold goods
    private final Map<Goods, Integer> soldGoods;
    // Map with the goods object and the price of the goods
    private final Map<Goods, Double> goodsPrices;
    private double totalPrice;
    private final int shopId;
    private final String shopName;
    // The shops object
    private final Shop shop;

    // The names of the folders that the files will be saved and read in
    private static final String RECEIPT_FOLDER = "Receipts_txt";
    private static final String SERIALIZED_RECEIPT_FOLDER = "Receipts_serialized";
    private static final String SHOP_INFO_FOLDER = "Shop_Info";

    public Receipt(int shopId, String shopName, Cashier cashier, int receiptId, Shop shop) {
        // Initialize the instance variables
        this.shopId = shopId;
        this.shopName = shopName;
        this.receiptId = receiptId;
        this.cashier = cashier;
        this.dateTime = LocalDateTime.now();
        this.soldGoods = new HashMap<>();
        this.goodsPrices = new HashMap<>();
        this.totalPrice = 0.0;
        this.shop = shop;

        // If there are no existing folders with this name create them, if the system wasn't able to create
        // the folders with that name in that directory throw an exception
        try {
            File folder = new File(RECEIPT_FOLDER);
            if (!folder.exists() && !folder.mkdir()) {
                throw new IOException("Couldn't create the folder: " + RECEIPT_FOLDER);
            }

            File serializedFolder = new File(SERIALIZED_RECEIPT_FOLDER);
            if (!serializedFolder.exists() && !serializedFolder.mkdir()) {
                throw new IOException("Couldn't create the folder: " + SERIALIZED_RECEIPT_FOLDER);
            }

            File shopInfoFolder = new File(SHOP_INFO_FOLDER);
            if (!shopInfoFolder.exists() && !shopInfoFolder.mkdir()) {
                throw new IOException("Couldn't create the folder: " + SHOP_INFO_FOLDER);
            }
        } catch (IOException e) {
            System.err.println("Error while trying to create folder: " + e.getMessage());
        }
    }

    // Setters for the variables used outside the class
    public int getReceiptId() { return receiptId; }
    public int getShopId() { return shopId; }
    public Cashier getCashier() { return cashier; }
    public double getTotalPrice() { return totalPrice; }
    public String getShopName() { return shopName; }
    // Format the date and return it
    public String getFormatDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return dateTime.format(formatter);
    }

    // Add the sale of the goods
    public void addSaleOfGoods(Goods goods, int saleQantity, double price) throws InsufficientQuantityException {
        // Check if the stores goods quantity is less than the sold goods
        if (goods.getQuantity() < saleQantity) {
            // Throw custom exception in case there are not enough goods to sell
            throw new InsufficientQuantityException("Insufficient quantity of these goods: " + goods.getName());
        }
        // Remove the sold quantity from the quantity that the store has
        goods.setQuantity(goods.getQuantity() - saleQantity);
        // Add the quantity of sold current goods in the map
        soldGoods.put(goods, saleQantity);
        // Add the price of the goods in the prices map
        goodsPrices.put(goods, price);
        // Add the price of all sold goods to the total price
        totalPrice += price * saleQantity;
    }

    // Save receipt to a TXT file inside the Receipts_txt folder
    public void saveReceiptToFile() throws IOException {
        // Build the file and file name path
        String fileName = RECEIPT_FOLDER + "/" + shopId + "_receipt_" + receiptId + ".txt";

        // Create and open the file to start writhing in it
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            // Write the needed information in the file
            writer.write(shopName + "\n");
            writer.write("Receipt number: " + receiptId + "\n");
            writer.write("Cashier ID: " + cashier.getId() + ", Name: " + cashier.getName() + "\n");
            writer.write("Date: " + getFormatDate() + "\n");
            writer.write("Items: \n");

            // For each sold item in the goods map write its sold quantity and its price it was sold for
            for (Map.Entry<Goods, Integer> entry : soldGoods.entrySet()) {
                // Get the current key from the map which is the object of the goods class
                Goods goods = entry.getKey();
                // Get the sold quantity of the current goods
                int quantity = entry.getValue();
                // Get the price of the current goods object
                double eachGoodsPrice = goodsPrices.get(goods);
                // Write the information of each sold item to the file
                writer.write(goods.getName() + " x " + quantity + " # " + String.format("%.2f", eachGoodsPrice) + " " + shop.getCurrency() + " per piece\n");
            }
            // Write the total price of all the sold goods in the file
            writer.write("Total Price: " + String.format("%.2f", totalPrice) + " " + shop.getCurrency() + "\n");
        }
    }

    // Read each of the details from of receipt txt file from the Receipts_txt folder
    public static void readReceiptFromFile(int shopId, int receiptId) throws IOException {
        // Create the file path using the passed Shop ID and receipt ID
        String fileName = RECEIPT_FOLDER + "/" + shopId + "_receipt_" + receiptId + ".txt";
        // Open the file and start reading from it
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            // Create a string that holds each line of the file as it is read
            String line;
            // Loop read and store each line in the line string from the file until there are no more lines in the file
            while ((line = reader.readLine()) != null) {
                // Print the current line in the console
                System.out.println(line);
            }
        }
    }

    // This method creates the serialized file that stores the object of the receipt
    public void serializeReceiptObj() throws IOException {
        // Build the file path for the current file
        String fileName = SERIALIZED_RECEIPT_FOLDER + "/" + shopId + "_receipt_" + receiptId + ".ser";
        // Open the .ser type file and store the object of the receipt in it
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(this);
        }
    }

    public static Receipt deserializeReceiptObj(int shopId, int receiptId) throws IOException, ClassNotFoundException {
        // Build the file path for the current file based on the passed shop ID and receipt ID
        String fileName = SERIALIZED_RECEIPT_FOLDER + "/" + shopId + "_receipt_" + receiptId + ".ser";
        // Open the file to read the serialized object and return the object
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            return (Receipt) ois.readObject();
        }
    }

    // This method updates the earned value of the shop in the shop information file
    public void updateShopTotalValue(int currentReceiptNumber) throws IOException {
        // Create the file path using the current shop ID
        String fileName = SHOP_INFO_FOLDER + "/" + shopId + "_shop_info.txt";

        // Call method that gets the last total value that was printed in the shop info file
        double currentTotalValue = getLastShopTotalValue(fileName);
        // and add the new receipt total price to the last total value of the shop
        currentTotalValue += totalPrice;

        // Update the new count of the total printed receipt numbers and the new updated total earned value of the shop
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(shopName + "\n");
            writer.write("Receipt number: " + currentReceiptNumber + "\n");
            writer.write("Total value: " + String.format("%.2f", currentTotalValue) + " " + shop.getCurrency() + "\n");
        }
    }

    private double getLastShopTotalValue(String fileName) throws IOException {
        double lastTotalValue = 0.0;
        File file = new File(fileName);

        // If the file with that directory exists open it
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Read each file of the line until the line that starts with Total value:  is found
                    if (line.startsWith("Total value: ")) {
                        // Trim the line until only the total value is left and turn it into a double
                        String totalValueStr = line.split(":")[1].trim().replace(" BGN", "").trim();
                        lastTotalValue = Double.parseDouble(totalValueStr);
                    }
                }
            }
        }

        // Return the updated current total value of the shop
        return lastTotalValue;
    }
}
