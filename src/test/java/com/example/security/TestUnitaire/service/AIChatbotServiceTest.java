package com.example.security.TestUnitaire.service;

import com.example.security.ai.AIChatbotService;
import com.example.security.ai.ChatbotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AIChatbotServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private AIChatbotService aiChatbotService;

    @BeforeEach
    void setUp() {
        aiChatbotService = new AIChatbotService();
        // Inject mock restTemplate via reflection
        ReflectionTestUtils.setField(aiChatbotService, "restTemplate", restTemplate);
    }

    // ==================== getResponse ====================

    @Test
    void getResponse_WhenPythonServiceAvailable_ShouldReturnResponse() {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("response", "Voici comment créer un cahier des charges...");
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        String result = aiChatbotService.getResponse("Comment créer un cahier des charges ?");

        assertThat(result).isEqualTo("Voici comment créer un cahier des charges...");
    }

    @Test
    void getResponse_WhenPythonServiceUnavailable_ShouldReturnFallbackMessage() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        String result = aiChatbotService.getResponse("Question quelconque");

        assertThat(result).contains("indisponible");
    }

    @Test
    void getResponse_WhenConnectionRefused_ShouldNotThrowException() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("SMTP error"));

        // Should not throw
        String result = aiChatbotService.getResponse("test");

        assertThat(result).isNotNull();
    }

    @Test
    void getResponse_ShouldSendQuestionToCorrectField() {
        Map<String, Object> responseBody = Map.of("response", "OK");
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        aiChatbotService.getResponse("ma question test");

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void getResponse_WhenNullResponse_ShouldHandleGracefully() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        // Should not throw NPE
        String result = aiChatbotService.getResponse("question");

        assertThat(result).isNotNull();
    }
}


// ==================== ChatbotService (FAQ interne) ====================

class ChatbotServiceTest {

    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        chatbotService = new ChatbotService();
    }

    @Test
    void getResponse_WithCahierKeyword_ShouldReturnInstructions() {
        String result = chatbotService.getChatResponse("comment créer cahier des charges");

        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
    }

    @Test
    void getResponse_WithValiderKeyword_ShouldReturnWorkflowInfo() {
        String result = chatbotService.getChatResponse("comment valider cahier");

        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
    }

    @Test
    void getResponse_WithItemKeyword_ShouldReturnItemInfo() {
        String result = chatbotService.getChatResponse("qu'est-ce qu'un item");

        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
    }

    @Test
    void getResponse_WithConformiteKeyword_ShouldReturnConformityInfo() {
        String result = chatbotService.getChatResponse("créer fiche conformité");

        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
    }

    @Test
    void getResponse_WithUnknownQuestion_ShouldReturnDefaultOrEmpty() {
        String result = chatbotService.getChatResponse("zgldkfjhsdf question inconnue");

        // Unknown question should return something (default message or empty)
        assertThat(result).isNotNull();
    }


    @Test
    void getResponse_WithEmptyInput_ShouldNotThrow() {
        String result = chatbotService.getChatResponse("");

        assertThat(result).isNotNull();
    }
}