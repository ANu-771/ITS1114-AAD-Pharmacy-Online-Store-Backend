package lk.ijse.pharmacy_backend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.ijse.pharmacy_backend.dto.chat.ChatRequestDTO;
import lk.ijse.pharmacy_backend.dto.chat.ChatResponseDTO;
import lk.ijse.pharmacy_backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String modelName;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String apiUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.create();

    private static final String SYSTEM_INSTRUCTION = 
            "You are MediMate, the official AI Clinical Pharmacist & Customer Healthcare Assistant for KK PHARMACY (Sri Lanka).\n" +
            "CRITICAL CONVERSATION GUIDELINES:\n" +
            "1. CONCISE & DIRECT: Keep replies SHORT, helpful, and friendly (strictly 2 to 4 sentences or a brief bulleted list). Never write long essays or unprompted lectures.\n" +
            "2. GREETINGS: If the user greets (e.g. 'hi', 'hello', 'hey'), reply warmly in 1-2 friendly sentences offering assistance.\n" +
            "3. CLINICAL ACCURACY: Provide brief, accurate dosage, indications, or active ingredient guidance.\n" +
            "4. PRESCRIPTION (Rx): Mention prescription verification ONLY when the user asks about prescription-only medicines or uploading an Rx.\n" +
            "5. EMERGENCIES: For acute medical emergencies, advise calling 1990 immediately in one brief sentence.\n" +
            "6. STORE & DELIVERY: KK PHARMACY provides free islandwide delivery in Sri Lanka over Rs. 5,000. Mention delivery or cold-chain storage ONLY when relevant to the user's inquiry.\n" +
            "7. FORMATTING: Use clean, concise markdown with bold highlights for key points.";

    @Override
    public ChatResponseDTO processMessage(ChatRequestDTO request) {
        // 1. Strict Check: If API key is not provided, return honest unavailable status (NO FAKE MOCK FALLBACK)
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("YOUR_GEMINI_API_KEY")) {
            log.warn("Gemini API key is not configured. MediMate AI Assistant is unavailable.");
            return ChatResponseDTO.builder()
                    .reply("⚠️ MediMate AI Assistant is currently unavailable. For medical advice, product availability, or prescription orders, please contact our licensed pharmacists directly at +94 11 234 5678 or support@kkpharmacy.com.")
                    .status("UNAVAILABLE")
                    .available(false)
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        try {
            // 2. Build Gemini REST API Request Body
            String endpointUrl = String.format("%s/%s:generateContent?key=%s", apiUrl, modelName, apiKey.trim());

            Map<String, Object> payload = new HashMap<>();

            // System Instruction
            Map<String, Object> sysInstr = new HashMap<>();
            sysInstr.put("parts", List.of(Map.of("text", SYSTEM_INSTRUCTION)));
            payload.put("systemInstruction", sysInstr);

            // Contents (Conversation History + Current Query)
            List<Map<String, Object>> contents = new ArrayList<>();
            if (request.getHistory() != null && !request.getHistory().isEmpty()) {
                // Keep last 4 messages to preserve fast generation token window
                int startIdx = Math.max(0, request.getHistory().size() - 4);
                for (int i = startIdx; i < request.getHistory().size(); i++) {
                    ChatRequestDTO.ChatMessageDTO h = request.getHistory().get(i);
                    if (h.getText() != null && !h.getText().isBlank()) {
                        String role = "user".equalsIgnoreCase(h.getRole()) ? "user" : "model";
                        contents.add(Map.of(
                                "role", role,
                                "parts", List.of(Map.of("text", h.getText()))
                        ));
                    }
                }
            }

            // Current message
            contents.add(Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", request.getMessage().trim()))
            ));
            payload.put("contents", contents);

            // Fast generation parameters (low token limit for quick replies)
            payload.put("generationConfig", Map.of(
                    "temperature", 0.3,
                    "maxOutputTokens", 250
            ));

            String requestJson = objectMapper.writeValueAsString(payload);

            // 3. Call Google Generative AI API securely from server
            String responseJson = restClient.post()
                    .uri(endpointUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestJson)
                    .retrieve()
                    .body(String.class);

            // 4. Parse response text
            JsonNode rootNode = objectMapper.readTree(responseJson);
            JsonNode candidates = rootNode.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    String aiText = parts.get(0).path("text").asText("");
                    return ChatResponseDTO.builder()
                            .reply(aiText)
                            .status("SUCCESS")
                            .available(true)
                            .timestamp(LocalDateTime.now())
                            .build();
                }
            }

            throw new RuntimeException("Empty or malformed candidate response from Gemini");

        } catch (Exception e) {
            log.error("Failed to generate AI response from Gemini: {}", e.getMessage());
            return ChatResponseDTO.builder()
                    .reply("⚠️ MediMate AI Assistant is temporarily unavailable due to a service connectivity timeout. For immediate healthcare assistance, please call our pharmacy hotline at +94 11 234 5678.")
                    .status("UNAVAILABLE")
                    .available(false)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    public ChatResponseDTO getStatus() {
        boolean isAvailable = apiKey != null && !apiKey.trim().isEmpty() && !apiKey.contains("YOUR_GEMINI_API_KEY");
        return ChatResponseDTO.builder()
                .reply(isAvailable ? "MediMate AI is ready and online." : "MediMate AI is currently not available.")
                .status(isAvailable ? "SUCCESS" : "UNAVAILABLE")
                .available(isAvailable)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
