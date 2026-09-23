package br.com.requestmngr.purchaserequest.service;

public class PurchaseRequestNotFoundException extends RuntimeException {

    public PurchaseRequestNotFoundException(long id) {
        super("Purchase request not found: " + id);
    }
}
