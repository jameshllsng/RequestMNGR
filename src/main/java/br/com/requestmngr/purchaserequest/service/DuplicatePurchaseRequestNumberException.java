package br.com.requestmngr.purchaserequest.service;

public class DuplicatePurchaseRequestNumberException extends RuntimeException {

    public DuplicatePurchaseRequestNumberException() {
        super("Purchase request number already exists.");
    }
}
