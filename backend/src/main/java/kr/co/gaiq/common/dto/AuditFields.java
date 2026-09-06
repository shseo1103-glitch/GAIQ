package kr.co.gaiq.common.dto;

import java.time.Instant;

public record AuditFields(Instant createdAt, Instant updatedAt) {
}
