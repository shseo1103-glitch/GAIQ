package kr.co.gaiq.batch.repository;

import java.time.Instant;
import kr.co.gaiq.batch.entity.SynthesisBatch;
import org.springframework.data.jpa.domain.Specification;

/**
 * Dynamic search predicates for {@link SynthesisBatch}.
 *
 * <p>Replaces the previous "(:param is null or ...)" JPQL pattern, which fails against
 * PostgreSQL because the driver cannot infer a bind parameter's type when it is compared
 * with a plain {@code IS NULL} check (ERROR: could not determine data type of parameter).
 * Specifications only add a predicate when the filter value is present, which sidesteps
 * the issue entirely and is the more idiomatic Spring Data JPA approach for optional
 * search criteria.
 */
public final class SynthesisBatchSpecifications {

    private SynthesisBatchSpecifications() {
    }

    public static Specification<SynthesisBatch> search(
            Long orgId, String batchNo, String status, Instant startDate, Instant endDate) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (orgId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("orgId"), orgId));
            }
            if (batchNo != null && !batchNo.isBlank()) {
                predicate = cb.and(predicate, cb.like(root.get("batchNo"), "%" + batchNo + "%"));
            }
            if (status != null && !status.isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            if (startDate != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }
            if (endDate != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }
            return predicate;
        };
    }
}
