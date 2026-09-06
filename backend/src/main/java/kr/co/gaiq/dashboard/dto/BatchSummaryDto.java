package kr.co.gaiq.dashboard.dto;

public record BatchSummaryDto(long planned, long running, long completed, long failed) {
}
