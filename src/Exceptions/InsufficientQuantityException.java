package Exceptions;

// Create custom Exception Insufficient quantity as it is used more than once
public class InsufficientQuantityException extends Exception {
  public InsufficientQuantityException(String message) {
    super(message);
  }
}