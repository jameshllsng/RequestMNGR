package br.com.requestmngr.purchaserequest.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PurchaseRequestItem(
        Long id,
        Long purchaseRequestId,
        String description,
        String brandModel,
        int quantity,
        String supplierName,
        BigDecimal totalValue,
        LocalDate receivedOn,
        String checkedBy,
        PurchaseRequestItemStatus status,
        String notes,
        Instant createdAt,
        Instant updatedAt) {
}
