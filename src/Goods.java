import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

// Class that holds the management for the creation of the goods
class Goods implements Serializable {
    // Create a unique identifier for the serialized class
    @Serial
    private static final long serialVersionUID = 1L;
    // Declare the current goods ID
    private final int id;
    // Declare the current goods Name
    private final String name;
    // Declare the buying price of the goods
    private final double buyingPrice;
    // Declare the goods type
    private final GoodsType goodsType;
    // Declare the date of the expiry  of the goods
    private final LocalDate dateOfExpiry;
    //Declare the quantity of the goods
    private int quantity;

    public Goods(int id, String name, double buyingPrice, GoodsType goodsType, LocalDate dateOfExpiry, int quantity) {
        // Initialize the instance variables as the passed parameters values
        this.id = id;
        this.name = name;
        this.buyingPrice = buyingPrice;
        this.goodsType = goodsType;
        this.dateOfExpiry = dateOfExpiry;
        this.quantity = quantity;
    }

    // Make getters for the needed variables
    public int getId() { return id; }
    public String getName() { return name; }
    public double getBuyingPrice() { return buyingPrice; }
    public int getQuantity() { return quantity; }
    // Set the quantity
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double calcSellingPrice(Shop shop) {
        if (LocalDate.now().isAfter(dateOfExpiry)) {
            throw new IllegalArgumentException("Goods that are expired cannot be sold: " + name);
        }

        // Determine the overcharge percentage based on the category of the goods
        double overchargePercentage = (goodsType == GoodsType.FOOD) ? shop.getPercentageOverchargeFood() : shop.getPercentageOverchargeNonFood();

        // Calculate the base selling price base on the buying price with the overcharge percentage added
        double sellingPrice = buyingPrice * (1 + overchargePercentage / 100);

        // Get the expiry discount based on the category and the expiry date
        long daysToExpire = ChronoUnit.DAYS.between(LocalDate.now(), dateOfExpiry);
        if ((goodsType == GoodsType.FOOD && daysToExpire < shop.getDaysTillExpiryFood()) ||
                (goodsType == GoodsType.NON_FOOD && daysToExpire < shop.getDaysTillExpiryNonFood())) {

            // Add the discount to the price
            double decreasePercentage = (goodsType == GoodsType.FOOD) ? shop.getPercentageDecreaseFood() : shop.getPercentageDecreaseNonFood();
            sellingPrice *= (1 - decreasePercentage / 100);
        }

        // Return the calculated selling price of the goods
        return sellingPrice;
    }

}