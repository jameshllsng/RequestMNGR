package br.com.requestmngr.purchaserequest.api;

import br.com.requestmngr.purchaserequest.domain.Priority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record UpdatePurchaseRequestRequest(
        @NotBlank @Size(max = 50) String requestNumber,
        @NotBlank @Size(max = 120) String department,
        @NotBlank @Size(max = 150) String requesterName,
        @NotNull @PastOrPresent LocalDate requestedOn,
        @NotBlank String purchaseReason,
        @NotNull Priority priority,
        @Size(max = 150) String buyerName,
        @Size(max = 150) String managerName,
        @Size(max = 80) String costCenter,
        @Size(max = 5000) String notes,
        @NotNull @Size(min = 1) List<@Valid CreatePurchaseRequestItemRequest> items) {
}
