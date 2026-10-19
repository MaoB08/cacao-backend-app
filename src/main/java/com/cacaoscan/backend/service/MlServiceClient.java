package com.cacaoscan.backend.service;

import com.cacaoscan.backend.dto.DiagnosticoDTOs.MlPredictionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Service
public class MlServiceClient {

    private static final Logger log = LoggerFactory.getLogger(MlServiceClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.ml-service.url:http://localhost:8000}")
    private String mlServiceUrl;

    @Value("${app.ml-service.timeout-seconds:30}")
    private int timeoutSeconds;

    @Value("${app.ml-service.api-key:}")
    private String apiKey;

    public MlServiceClient(ObjectMapper objectMapper, RestTemplateBuilder restTemplateBuilder) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * Reenvía la imagen al microservicio de inferencia neuronal y devuelve la predicción.
     */
    public MlPredictionResponse predict(MultipartFile file, String requestId) {
        String url = mlServiceUrl + "/v1/predictions";
        log.info("Enviando imagen al servicio ML [requestId={}, size={} bytes, type={}]",
                requestId, file.getSize(), file.getContentType());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            if (apiKey != null && !apiKey.isBlank()) {
                headers.set("X-API-Key", apiKey);
            }

            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "mazorca.jpg";
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };

            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.parseMediaType(
                    file.getContentType() != null ? file.getContentType() : MediaType.IMAGE_JPEG_VALUE));

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new HttpEntity<>(resource, fileHeaders));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<MlPredictionResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, MlPredictionResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                MlPredictionResponse pred = response.getBody();
                log.info("Predicción recibida [requestId={}, topClass={}, prob={}, status={}]",
                        requestId,
                        pred.getTopPrediction() != null ? pred.getTopPrediction().getClassCode() : "N/A",
                        pred.getTopPrediction() != null ? pred.getTopPrediction().getProbability() : 0.0,
                        pred.getStatus());
                return pred;
            }

            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Respuesta inesperada del servicio de análisis neuronal: " + response.getStatusCode());
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            log.error("Error al conectar con el servicio ML [requestId={}]: {}", requestId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El servicio de análisis neuronal no está disponible actualmente. Intente más tarde.", e);
        }
    }

    public boolean isHealthy() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(mlServiceUrl + "/health", String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Servicio ML no responde en /health: {}", e.getMessage());
            return false;
        }
    }
}
