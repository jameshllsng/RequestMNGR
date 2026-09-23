package br.com.requestmngr.purchaserequest.repository;

import br.com.requestmngr.purchaserequest.domain.PurchaseRequestItem;
import br.com.requestmngr.purchaserequest.domain.PurchaseRequestItemStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcPurchaseRequestItemRepository implements PurchaseRequestItemRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPurchaseRequestItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PurchaseRequestItem insert(PurchaseRequestItem purchaseRequestItem) {
        String sql = """
                INSERT INTO purchase_request_items (
                    purchase_request_id, description, brand_model, quantity, supplier_name,
                    total_value, received_on, checked_by, status, notes
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id, created_at, updated_at
                """;

        return jdbcTemplate.queryForObject(sql, (resultSet, rowNumber) -> new PurchaseRequestItem(
                        resultSet.getLong("id"),
                        purchaseRequestItem.purchaseRequestId(),
                        purchaseRequestItem.description(),
                        purchaseRequestItem.brandModel(),
                        purchaseRequestItem.quantity(),
                        purchaseRequestItem.supplierName(),
                        purchaseRequestItem.totalValue(),
                        purchaseRequestItem.receivedOn(),
                        purchaseRequestItem.checkedBy(),
                        purchaseRequestItem.status(),
                        purchaseRequestItem.notes(),
                        resultSet.getTimestamp("created_at").toInstant(),
                        resultSet.getTimestamp("updated_at").toInstant()),
                purchaseRequestItem.purchaseRequestId(),
                purchaseRequestItem.description(),
                purchaseRequestItem.brandModel(),
                purchaseRequestItem.quantity(),
                purchaseRequestItem.supplierName(),
                purchaseRequestItem.totalValue(),
                purchaseRequestItem.receivedOn(),
                purchaseRequestItem.checkedBy(),
                purchaseRequestItem.status().name(),
                purchaseRequestItem.notes());
    }

    @Override
    public List<PurchaseRequestItem> findByPurchaseRequestId(long purchaseRequestId) {
        String sql = """
                SELECT id, purchase_request_id, description, brand_model, quantity, supplier_name,
                       total_value, received_on, checked_by, status, notes, created_at, updated_at
                FROM purchase_request_items
                WHERE purchase_request_id = ?
                ORDER BY id
                """;

        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> new PurchaseRequestItem(
                resultSet.getLong("id"),
                resultSet.getLong("purchase_request_id"),
                resultSet.getString("description"),
                resultSet.getString("brand_model"),
                resultSet.getInt("quantity"),
                resultSet.getString("supplier_name"),
                resultSet.getBigDecimal("total_value"),
                resultSet.getObject("received_on", java.time.LocalDate.class),
                resultSet.getString("checked_by"),
                PurchaseRequestItemStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("notes"),
                resultSet.getTimestamp("created_at").toInstant(),
                resultSet.getTimestamp("updated_at").toInstant()), purchaseRequestId);
    }
}
