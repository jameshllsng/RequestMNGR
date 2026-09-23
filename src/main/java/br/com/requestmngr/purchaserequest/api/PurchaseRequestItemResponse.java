package br.com.requestmngr.purchaserequest.api;

import br.com.requestmngr.purchaserequest.domain.PurchaseRequestItemStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseRequestItemResponse(
        Long id,
        String description,
        String brandModel,
        int quantity,
        String supplierName,
        BigDecimal totalValue,
        LocalDate receivedOn,
        String checkedBy,
        PurchaseRequestItemStatus status,
        String notes) {
}
