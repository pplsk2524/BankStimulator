package main.java.com.banking.exception;

public class DuplicateAccountException extends Exception {
    public DuplicateAccountException(String message) {
        super(message);
    }
}
