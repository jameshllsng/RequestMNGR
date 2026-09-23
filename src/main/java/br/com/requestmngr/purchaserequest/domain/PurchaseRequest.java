package br.com.requestmngr.purchaserequest.domain;

import java.time.Instant;
import java.time.LocalDate;

public record PurchaseRequest(
        Long id,
        String requestNumber,
        String department,
        String requesterName,
        LocalDate requestedOn,
        String purchaseReason,
        Priority priority,
        String buyerName,
        String managerName,
        String costCenter,
        PurchaseRequestStatus status,
        String notes,
        Instant createdAt,
        Instant updatedAt) {
}
