package br.com.requestmngr.purchaserequest.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseRequestSummary(
        Long id,
        String requestNumber,
        String department,
        String requesterName,
        String purchaseReason,
        Priority priority,
        PurchaseRequestStatus status,
        LocalDate requestedOn,
        int itemCount,
        BigDecimal totalValue) {
}
