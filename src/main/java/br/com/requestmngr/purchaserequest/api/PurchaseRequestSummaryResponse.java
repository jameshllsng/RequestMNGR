package br.com.requestmngr.purchaserequest.api;

import br.com.requestmngr.purchaserequest.domain.Priority;
import br.com.requestmngr.purchaserequest.domain.PurchaseRequestStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseRequestSummaryResponse(
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
