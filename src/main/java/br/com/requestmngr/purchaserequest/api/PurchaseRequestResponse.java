package br.com.requestmngr.purchaserequest.api;

import br.com.requestmngr.purchaserequest.domain.Priority;
import br.com.requestmngr.purchaserequest.domain.PurchaseRequestStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PurchaseRequestResponse(
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
        BigDecimal totalValue,
        List<PurchaseRequestItemResponse> items,
        Instant createdAt,
        Instant updatedAt) {
}
