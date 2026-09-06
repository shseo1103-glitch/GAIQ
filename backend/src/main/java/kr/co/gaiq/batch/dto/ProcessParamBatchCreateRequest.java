package kr.co.gaiq.batch.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ProcessParamBatchCreateRequest(
        @NotEmpty @Valid List<SynthesisProcessParamCreateRequest> params) {
}
