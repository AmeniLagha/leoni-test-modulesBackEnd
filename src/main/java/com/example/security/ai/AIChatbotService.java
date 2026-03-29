package com.example.security.ai;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AIChatbotService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String pythonServiceUrl = "http://localhost:5000/ask";

    public String getResponse(String userMessage) {
        try {
            Map<String, String> request = new HashMap<>();
            request.put("question", userMessage);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    pythonServiceUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            return (String) response.getBody().get("response");

        } catch (Exception e) {
            System.err.println("❌ Erreur appel IA: " + e.getMessage());
            return "⚠️ Service IA temporairement indisponible. Veuillez réessayer plus tard.";
        }
    }
}