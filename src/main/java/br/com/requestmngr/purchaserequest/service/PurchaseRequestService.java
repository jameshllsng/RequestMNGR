package br.com.requestmngr.purchaserequest.service;

import br.com.requestmngr.purchaserequest.api.CreatePurchaseRequestItemRequest;
import br.com.requestmngr.purchaserequest.api.CreatePurchaseRequestRequest;
import br.com.requestmngr.purchaserequest.api.PurchaseRequestItemResponse;
import br.com.requestmngr.purchaserequest.api.PurchaseRequestResponse;
import br.com.requestmngr.purchaserequest.api.PurchaseRequestSummaryResponse;
import br.com.requestmngr.purchaserequest.api.UpdatePurchaseRequestRequest;
import br.com.requestmngr.purchaserequest.domain.PurchaseRequest;
import br.com.requestmngr.purchaserequest.domain.PurchaseRequestItem;
import br.com.requestmngr.purchaserequest.domain.PurchaseRequestItemStatus;
import br.com.requestmngr.purchaserequest.domain.PurchaseRequestStatus;
import br.com.requestmngr.purchaserequest.domain.PurchaseRequestSummary;
import br.com.requestmngr.purchaserequest.repository.PurchaseRequestItemRepository;
import br.com.requestmngr.purchaserequest.repository.PurchaseRequestRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PurchaseRequestService {

    private final PurchaseRequestRepository purchaseRequestRepository;
    private final PurchaseRequestItemRepository purchaseRequestItemRepository;

    public PurchaseRequestService(
            PurchaseRequestRepository purchaseRequestRepository,
            PurchaseRequestItemRepository purchaseRequestItemRepository) {
        this.purchaseRequestRepository = purchaseRequestRepository;
        this.purchaseRequestItemRepository = purchaseRequestItemRepository;
    }

    @Transactional
    public PurchaseRequestResponse create(CreatePurchaseRequestRequest request) {
        try {
            PurchaseRequest savedRequest = purchaseRequestRepository.insert(new PurchaseRequest(
                    null,
                    request.requestNumber(),
                    request.department(),
                    request.requesterName(),
                    request.requestedOn(),
                    request.purchaseReason(),
                    request.priority(),
                    request.buyerName(),
                    request.managerName(),
                    request.costCenter(),
                    PurchaseRequestStatus.OPEN,
                    request.notes(),
                    null,
                    null));

            request.items().forEach(item -> purchaseRequestItemRepository.insert(toPurchaseRequestItem(savedRequest.id(), item)));
            return getById(savedRequest.id());
        } catch (DuplicateKeyException exception) {
            throw new DuplicatePurchaseRequestNumberException();
        }
    }

    @Transactional(readOnly = true)
    public List<PurchaseRequestSummaryResponse> findAll() {
        return purchaseRequestRepository.findAll().stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PurchaseRequestResponse getById(long id) {
        PurchaseRequest purchaseRequest = purchaseRequestRepository.findById(id)
                .orElseThrow(() -> new PurchaseRequestNotFoundException(id));
        List<PurchaseRequestItem> items = purchaseRequestItemRepository.findByPurchaseRequestId(id);

        return toResponse(purchaseRequest, items);
    }

    @Transactional
    public PurchaseRequestResponse update(long id, UpdatePurchaseRequestRequest request) {
        PurchaseRequest currentRequest = purchaseRequestRepository.findById(id)
                .orElseThrow(() -> new PurchaseRequestNotFoundException(id));

        try {
            PurchaseRequest updatedRequest = purchaseRequestRepository.update(new PurchaseRequest(
                            currentRequest.id(),
                            request.requestNumber(),
                            request.department(),
                            request.requesterName(),
                            request.requestedOn(),
                            request.purchaseReason(),
                            request.priority(),
                            request.buyerName(),
                            request.managerName(),
                            request.costCenter(),
                            currentRequest.status(),
                            request.notes(),
                            currentRequest.createdAt(),
                            currentRequest.updatedAt()))
                    .orElseThrow(() -> new PurchaseRequestNotFoundException(id));

            return toResponse(updatedRequest, purchaseRequestItemRepository.findByPurchaseRequestId(id));
        } catch (DuplicateKeyException exception) {
            throw new DuplicatePurchaseRequestNumberException();
        }
    }

    private PurchaseRequestItem toPurchaseRequestItem(long purchaseRequestId, CreatePurchaseRequestItemRequest item) {
        return new PurchaseRequestItem(
                null,
                purchaseRequestId,
                item.description(),
                item.brandModel(),
                item.quantity(),
                item.supplierName(),
                item.totalValue(),
                null,
                null,
                PurchaseRequestItemStatus.PENDING,
                item.notes(),
                null,
                null);
    }

    private PurchaseRequestResponse toResponse(PurchaseRequest purchaseRequest, List<PurchaseRequestItem> items) {
        List<PurchaseRequestItemResponse> itemResponses = items.stream()
                .map(item -> new PurchaseRequestItemResponse(
                        item.id(),
                        item.description(),
                        item.brandModel(),
                        item.quantity(),
                        item.supplierName(),
                        item.totalValue(),
                        item.receivedOn(),
                        item.checkedBy(),
                        item.status(),
                        item.notes()))
                .toList();

        BigDecimal totalValue = items.stream()
                .map(PurchaseRequestItem::totalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PurchaseRequestResponse(
                purchaseRequest.id(),
                purchaseRequest.requestNumber(),
                purchaseRequest.department(),
                purchaseRequest.requesterName(),
                purchaseRequest.requestedOn(),
                purchaseRequest.purchaseReason(),
                purchaseRequest.priority(),
                purchaseRequest.buyerName(),
                purchaseRequest.managerName(),
                purchaseRequest.costCenter(),
                purchaseRequest.status(),
                purchaseRequest.notes(),
                totalValue,
                itemResponses,
                purchaseRequest.createdAt(),
                purchaseRequest.updatedAt());
    }

    private PurchaseRequestSummaryResponse toSummaryResponse(PurchaseRequestSummary summary) {
        return new PurchaseRequestSummaryResponse(
                summary.id(),
                summary.requestNumber(),
                summary.department(),
                summary.requesterName(),
                summary.purchaseReason(),
                summary.priority(),
                summary.status(),
                summary.requestedOn(),
                summary.itemCount(),
                summary.totalValue());
    }
}
