package com.example.security.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ChatbotController {

   private final AIChatbotService aiChatbotService;

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> askQuestion(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        String response = aiChatbotService.getResponse(question);

        Map<String, String> result = new HashMap<>();
        result.put("response", response);
        return ResponseEntity.ok(result);
    }
}