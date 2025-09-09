import Exceptions.InsufficientFundsException;

// This class holds the Clients management, Its only purpose is to hold the money the client has and to
// do a check if the money are less than the total money for the receipt and if they are to throw an exception
class Client {
    // Declare the money of the client
    private final double money;

    public Client(double money) {
        // Initialize the instance variable
        this.money = money;
    }

    // Method that checks if the client has enough money to buy the goods
    public void checkIsEnoughMoney(double totalPrice) throws InsufficientFundsException {
        if (this.money < totalPrice) {
            // If the client doesn't have enough money throw a custom exception
            throw new InsufficientFundsException("Not enough funds. Total amount to pay is: " + this.money + ", Total amount needed:" + totalPrice);
        }
    }
}