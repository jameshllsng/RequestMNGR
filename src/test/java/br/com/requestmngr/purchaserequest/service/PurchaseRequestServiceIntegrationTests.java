package br.com.requestmngr.purchaserequest.service;

import br.com.requestmngr.purchaserequest.api.CreatePurchaseRequestItemRequest;
import br.com.requestmngr.purchaserequest.api.CreatePurchaseRequestRequest;
import br.com.requestmngr.purchaserequest.api.PurchaseRequestResponse;
import br.com.requestmngr.purchaserequest.domain.Priority;
import br.com.requestmngr.purchaserequest.domain.PurchaseRequestItemStatus;
import br.com.requestmngr.purchaserequest.domain.PurchaseRequestStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PurchaseRequestServiceIntegrationTests {

    @Autowired
    private PurchaseRequestService purchaseRequestService;

    @Test
    void createsPurchaseRequestWithItemsAndCalculatesTotalValue() {
        PurchaseRequestResponse response = purchaseRequestService.create(new CreatePurchaseRequestRequest(
                "REQ-" + UUID.randomUUID(),
                "Infrastructure",
                "Taylor Morgan",
                LocalDate.now(),
                "Replace office network equipment.",
                Priority.HIGH,
                null,
                null,
                "CC-100",
                "Fictional test data.",
                List.of(
                        new CreatePurchaseRequestItemRequest(
                                "Network switch",
                                "Example model",
                                2,
                                "Example Supplier",
                                new BigDecimal("1200.00"),
                                null),
                        new CreatePurchaseRequestItemRequest(
                                "Network cable",
                                null,
                                10,
                                null,
                                new BigDecimal("150.50"),
                                null))));

        assertThat(response.id()).isNotNull();
        assertThat(response.status()).isEqualTo(PurchaseRequestStatus.OPEN);
        assertThat(response.items()).hasSize(2);
        assertThat(response.items()).allSatisfy(item -> assertThat(item.status())
                .isEqualTo(PurchaseRequestItemStatus.PENDING));
        assertThat(response.totalValue()).isEqualByComparingTo("1350.50");
    }
}
