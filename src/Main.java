import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Create a shop object and pass the arguments:
        // Shop ID, Shop Name, Shop Currency, Overcharge FOOD, Overcharge NON_FOOD, Decrease FOOD, Decrease NON_FOOD
        // Days the goods are good for FOOD, Days the goods are good for NON_FOOD
        Shop shop = new Shop(1, "GoodsShop", "BGN", 20, 40, 10, 5, 7, 10);

        // Create the cashiers objects and pass the arguments:
        // Cashier ID, Cashier Name, Cashier Salary
        Cashier cashier = new Cashier(1, "Kara Clark", 1500);
        Cashier cashier2 = new Cashier(2, "Sara Tara", 1000);
        // Add the created cashiers to the cashier list in the shop object
        shop.addCashierToList(cashier);
        shop.addCashierToList(cashier2);

        // Create the goods objects and pass the arguments:
        // Goods ID, Goods Name, Goods buying price, Goods type Food/Non-food, and the date they expire
        Goods apple = new Goods(101, "Strawberry", 2.0, GoodsType.FOOD, LocalDate.now().plusDays(4), 300);
        Goods soap = new Goods(102, "Pants", 50.0, GoodsType.NON_FOOD, LocalDate.now().plusDays(30), 4);
        shop.addGoodsToList(apple);
        shop.addGoodsToList(soap);

        // Create a map that holds the key that is the id of the goods to buy and their quantity
        Map<Integer, Integer> itemsToBuy = new HashMap<>();
        itemsToBuy.put(101, 100);
        itemsToBuy.put(102, 3);

        // Create the client object that holds the money the client has
        Client client = new Client(5000);

        // Create the receipt
        Receipt receipt = shop.sellGoods(cashier, itemsToBuy, client);

        if (receipt != null) {
            try {
                // Call method that opens and prints the information from the txt receipt file
                Receipt.readReceiptFromFile(receipt.getShopId(), receipt.getReceiptId());

                // Call method to deserialize the receipt from the serialized receipt file and print some data to test it
                Receipt deserializedReceipt = Receipt.deserializeReceiptObj(receipt.getShopId(), receipt.getReceiptId());
                System.out.println("\nDeserialized Receipt:");
                System.out.println("Shop: " + deserializedReceipt.getShopName());
                System.out.println("Receipt ID: " + deserializedReceipt.getReceiptId());
                System.out.println("Cashier: " + deserializedReceipt.getCashier().getName());
                // Print the total price and format it to have two numbers after the decimal
                System.out.println("Total Price: " + String.format("%.2f", deserializedReceipt.getTotalPrice()) + " " + shop.getCurrency());

                // Throw error in case there are problems with the deserialization
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }

        // Print the total profit of the shop
        System.out.println("\n" + shop.getName() + " Total Profit: " + String.format("%.2f", shop.calculateProfit()) + " " + shop.getCurrency());
    }
}
