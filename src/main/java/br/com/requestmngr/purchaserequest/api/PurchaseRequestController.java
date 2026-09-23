package br.com.requestmngr.purchaserequest.api;

import br.com.requestmngr.purchaserequest.service.PurchaseRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-requests")
public class PurchaseRequestController {

    private final PurchaseRequestService purchaseRequestService;

    public PurchaseRequestController(PurchaseRequestService purchaseRequestService) {
        this.purchaseRequestService = purchaseRequestService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseRequestResponse create(@Valid @RequestBody CreatePurchaseRequestRequest request) {
        return purchaseRequestService.create(request);
    }

    @GetMapping
    public List<PurchaseRequestSummaryResponse> findAll() {
        return purchaseRequestService.findAll();
    }

    @GetMapping("/{id}")
    public PurchaseRequestResponse getById(@PathVariable long id) {
        return purchaseRequestService.getById(id);
    }

    @PutMapping("/{id}")
    public PurchaseRequestResponse update(
            @PathVariable long id,
            @Valid @RequestBody UpdatePurchaseRequestRequest request) {
        return purchaseRequestService.update(id, request);
    }
}
