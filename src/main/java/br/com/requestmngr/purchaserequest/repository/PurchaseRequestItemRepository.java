package br.com.requestmngr.purchaserequest.repository;

import br.com.requestmngr.purchaserequest.domain.PurchaseRequestItem;

import java.util.List;

public interface PurchaseRequestItemRepository {

    PurchaseRequestItem insert(PurchaseRequestItem purchaseRequestItem);

    List<PurchaseRequestItem> findByPurchaseRequestId(long purchaseRequestId);
}
