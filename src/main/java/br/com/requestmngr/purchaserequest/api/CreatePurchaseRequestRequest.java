package br.com.requestmngr.purchaserequest.api;

import br.com.requestmngr.purchaserequest.domain.Priority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CreatePurchaseRequestRequest(
        @NotBlank(message = "O número da requisição é obrigatório.")
        @Size(max = 50, message = "O número da requisição deve ter no máximo 50 caracteres.")
        String requestNumber,

        @NotBlank(message = "O setor é obrigatório.")
        @Size(max = 120, message = "O setor deve ter no máximo 120 caracteres.")
        String department,

        @NotBlank(message = "O solicitante é obrigatório.")
        @Size(max = 150, message = "O solicitante deve ter no máximo 150 caracteres.")
        String requesterName,

        @NotNull(message = "A data da solicitação é obrigatória.")
        @PastOrPresent(message = "A data da solicitação não pode estar no futuro.")
        LocalDate requestedOn,

        @NotBlank(message = "O motivo da compra é obrigatório.")
        String purchaseReason,

        @NotNull(message = "A prioridade é obrigatória.")
        Priority priority,

        @Size(max = 150, message = "O comprador deve ter no máximo 150 caracteres.")
        String buyerName,

        @Size(max = 150, message = "O gestor deve ter no máximo 150 caracteres.")
        String managerName,

        @Size(max = 80, message = "O centro de custo deve ter no máximo 80 caracteres.")
        String costCenter,

        @Size(max = 5000, message = "As observações devem ter no máximo 5000 caracteres.")
        String notes,

        @NotEmpty(message = "A requisição deve possuir ao menos um item.")
        List<@Valid CreatePurchaseRequestItemRequest> items) {
}
