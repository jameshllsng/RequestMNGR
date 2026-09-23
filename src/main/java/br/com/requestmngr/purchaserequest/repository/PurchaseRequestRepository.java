package br.com.requestmngr.purchaserequest.repository;

import br.com.requestmngr.purchaserequest.domain.PurchaseRequest;
import br.com.requestmngr.purchaserequest.domain.PurchaseRequestSummary;

import java.util.List;
import java.util.Optional;

public interface PurchaseRequestRepository {

    PurchaseRequest insert(PurchaseRequest purchaseRequest);

    Optional<PurchaseRequest> findById(long id);

    List<PurchaseRequestSummary> findAll();

    Optional<PurchaseRequest> update(PurchaseRequest purchaseRequest);
}
