package br.com.requestmngr.purchaserequest.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreatePurchaseRequestItemRequest(
        @NotBlank(message = "A descrição do item é obrigatória.")
        @Size(max = 500, message = "A descrição do item deve ter no máximo 500 caracteres.")
        String description,

        @Size(max = 250, message = "Marca ou modelo deve ter no máximo 250 caracteres.")
        String brandModel,

        @Positive(message = "A quantidade deve ser maior que zero.")
        int quantity,

        @Size(max = 200, message = "Fornecedor deve ter no máximo 200 caracteres.")
        String supplierName,

        @NotNull(message = "O valor total do item é obrigatório.")
        @DecimalMin(value = "0.00", message = "O valor total não pode ser negativo.")
        BigDecimal totalValue,

        @Size(max = 2000, message = "As observações do item devem ter no máximo 2000 caracteres.")
        String notes) {
}
