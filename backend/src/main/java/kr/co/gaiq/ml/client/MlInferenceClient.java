package kr.co.gaiq.ml.client;

import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * GAIQ ML 추론 FastAPI 서비스({@code ml/service/main.py}, 기본 포트 8115) 호출 클라이언트.
 *
 * <p>서비스가 다운되었거나 네트워크 오류가 발생하면 {@code null}을 반환하여 호출부
 * ({@link kr.co.gaiq.ml.service.MlPredictionService})가 기존 룰기반 폴백 로직으로
 * graceful degradation 할 수 있도록 한다 — 예외를 던지지 않는다.
 */
@Component
public class MlInferenceClient {

    private static final Logger log = LoggerFactory.getLogger(MlInferenceClient.class);

    private final RestClient restClient;

    public MlInferenceClient(
            @Value("${gaiq.ml.service-url:http://localhost:8115}") String serviceUrl,
            @Value("${gaiq.ml.timeout-ms:2000}") int timeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        requestFactory.setReadTimeout(Duration.ofMillis(timeoutMs));
        this.restClient = RestClient.builder()
                .baseUrl(serviceUrl)
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * @return 예측 응답. FastAPI 다운/타임아웃/오류 시 {@code null} (호출부가 폴백 처리).
     */
    public MlInferenceResponse predict(String targetMetricCode, Map<String, Object> inputParams) {
        try {
            return restClient.post()
                    .uri("/predict")
                    .body(new MlInferenceResponse.Request(targetMetricCode, inputParams))
                    .retrieve()
                    .body(MlInferenceResponse.class);
        } catch (RestClientException | IllegalStateException e) {
            log.warn(
                    "ML inference service unavailable for metric={}, falling back to rule-based logic: {}",
                    targetMetricCode, e.getMessage());
            return null;
        }
    }
}
