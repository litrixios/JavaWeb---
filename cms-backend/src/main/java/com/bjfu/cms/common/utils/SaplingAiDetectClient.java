package com.bjfu.cms.common.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Component
public class SaplingAiDetectClient {

    private final WebClient webClient;
    private final String apiKey;

    public SaplingAiDetectClient(
            @Value("${sapling.baseUrl:https://api.sapling.ai}") String baseUrl,
            @Value("${sapling.apiKey}") String apiKey
    ) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public double detectAiScore(String text) {
        // Sapling 单次 text 上限 200,000 字符（过长需要切分）:contentReference[oaicite:4]{index=4}
        String payloadText = text.length() > 200_000 ? text.substring(0, 200_000) : text;

        Map<String, Object> body = Map.of(
                "key", apiKey,
                "text", payloadText,
                "sent_scores", false,     // 只要总分更快 :contentReference[oaicite:5]{index=5}
                "score_string", false
        );

        return webClient.post()
                .uri("/api/v1/aidetect")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(20))
                .onErrorResume(e -> Mono.error(new RuntimeException("Sapling AI Detect 调用失败: " + e.getMessage(), e)))
                .map(resp -> {
                    Object scoreObj = resp.get("score");
                    if (scoreObj == null) throw new RuntimeException("Sapling 返回缺少 score 字段");
                    return ((Number) scoreObj).doubleValue();
                })
                .block();
    }
}
