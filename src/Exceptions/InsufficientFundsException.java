package Exceptions;

// Create custom Exception Insufficient funds as it is used more than once
public class InsufficientFundsException extends Exception {
  public InsufficientFundsException(String message) {
    super(message);
  }
}

