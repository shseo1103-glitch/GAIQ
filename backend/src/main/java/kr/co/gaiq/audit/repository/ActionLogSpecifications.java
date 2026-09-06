package kr.co.gaiq.audit.repository;

import java.time.Instant;
import kr.co.gaiq.audit.entity.ActionLog;
import org.springframework.data.jpa.domain.Specification;

/**
 * Dynamic search predicates for {@link ActionLog}.
 *
 * <p>See {@code kr.co.gaiq.batch.repository.SynthesisBatchSpecifications} for the rationale:
 * this replaces a "(:param is null or ...)" JPQL query that PostgreSQL rejects with
 * "could not determine data type of parameter" when the corresponding filter is not
 * supplied by the caller.
 */
public final class ActionLogSpecifications {

    private ActionLogSpecifications() {
    }

    public static Specification<ActionLog> search(
            String module, Long actorUserId, Instant startDate, Instant endDate) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (module != null && !module.isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.get("module"), module));
            }
            if (actorUserId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("actorUserId"), actorUserId));
            }
            if (startDate != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("occurredAt"), startDate));
            }
            if (endDate != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("occurredAt"), endDate));
            }
            return predicate;
        };
    }
}
