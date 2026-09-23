package br.com.requestmngr.purchaserequest.repository;

import br.com.requestmngr.purchaserequest.domain.Priority;
import br.com.requestmngr.purchaserequest.domain.PurchaseRequest;
import br.com.requestmngr.purchaserequest.domain.PurchaseRequestStatus;
import br.com.requestmngr.purchaserequest.domain.PurchaseRequestSummary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcPurchaseRequestRepository implements PurchaseRequestRepository {

    private static final RowMapper<PurchaseRequest> PURCHASE_REQUEST_ROW_MAPPER = new PurchaseRequestRowMapper();

    private final JdbcTemplate jdbcTemplate;

    public JdbcPurchaseRequestRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PurchaseRequest insert(PurchaseRequest purchaseRequest) {
        String sql = """
                INSERT INTO purchase_requests (
                    request_number, department, requester_name, requested_on, purchase_reason,
                    priority, buyer_name, manager_name, cost_center, status, notes
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;

        Long id = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                purchaseRequest.requestNumber(),
                purchaseRequest.department(),
                purchaseRequest.requesterName(),
                purchaseRequest.requestedOn(),
                purchaseRequest.purchaseReason(),
                purchaseRequest.priority().name(),
                purchaseRequest.buyerName(),
                purchaseRequest.managerName(),
                purchaseRequest.costCenter(),
                purchaseRequest.status().name(),
                purchaseRequest.notes());

        return findById(id).orElseThrow();
    }

    @Override
    public Optional<PurchaseRequest> findById(long id) {
        String sql = """
                SELECT id, request_number, department, requester_name, requested_on, purchase_reason,
                       priority, buyer_name, manager_name, cost_center, status, notes, created_at, updated_at
                FROM purchase_requests
                WHERE id = ?
                """;

        return jdbcTemplate.query(sql, PURCHASE_REQUEST_ROW_MAPPER, id).stream().findFirst();
    }

    @Override
    public List<PurchaseRequestSummary> findAll() {
        String sql = """
                SELECT request.id, request.request_number, request.department, request.requester_name,
                       request.purchase_reason, request.priority, request.status, request.requested_on,
                       COUNT(item.id) AS item_count,
                       COALESCE(SUM(item.total_value), 0) AS total_value
                FROM purchase_requests request
                LEFT JOIN purchase_request_items item ON item.purchase_request_id = request.id
                GROUP BY request.id
                ORDER BY request.created_at DESC
                """;

        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> new PurchaseRequestSummary(
                resultSet.getLong("id"),
                resultSet.getString("request_number"),
                resultSet.getString("department"),
                resultSet.getString("requester_name"),
                resultSet.getString("purchase_reason"),
                Priority.valueOf(resultSet.getString("priority")),
                PurchaseRequestStatus.valueOf(resultSet.getString("status")),
                resultSet.getObject("requested_on", java.time.LocalDate.class),
                resultSet.getInt("item_count"),
                resultSet.getBigDecimal("total_value")));
    }

    @Override
    public Optional<PurchaseRequest> update(PurchaseRequest purchaseRequest) {
        String sql = """
                UPDATE purchase_requests
                SET request_number = ?, department = ?, requester_name = ?, requested_on = ?,
                    purchase_reason = ?, priority = ?, buyer_name = ?, manager_name = ?,
                    cost_center = ?, notes = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        int updatedRows = jdbcTemplate.update(
                sql,
                purchaseRequest.requestNumber(),
                purchaseRequest.department(),
                purchaseRequest.requesterName(),
                purchaseRequest.requestedOn(),
                purchaseRequest.purchaseReason(),
                purchaseRequest.priority().name(),
                purchaseRequest.buyerName(),
                purchaseRequest.managerName(),
                purchaseRequest.costCenter(),
                purchaseRequest.notes(),
                purchaseRequest.id());

        return updatedRows == 0 ? Optional.empty() : findById(purchaseRequest.id());
    }

    private static class PurchaseRequestRowMapper implements RowMapper<PurchaseRequest> {

        @Override
        public PurchaseRequest mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
            return new PurchaseRequest(
                    resultSet.getLong("id"),
                    resultSet.getString("request_number"),
                    resultSet.getString("department"),
                    resultSet.getString("requester_name"),
                    resultSet.getObject("requested_on", java.time.LocalDate.class),
                    resultSet.getString("purchase_reason"),
                    Priority.valueOf(resultSet.getString("priority")),
                    resultSet.getString("buyer_name"),
                    resultSet.getString("manager_name"),
                    resultSet.getString("cost_center"),
                    PurchaseRequestStatus.valueOf(resultSet.getString("status")),
                    resultSet.getString("notes"),
                    resultSet.getTimestamp("created_at").toInstant(),
                    resultSet.getTimestamp("updated_at").toInstant());
        }
    }
}
