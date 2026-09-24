package lk.ijse.pharmacy_backend.controller;

import jakarta.validation.Valid;
import lk.ijse.pharmacy_backend.dto.chat.ChatRequestDTO;
import lk.ijse.pharmacy_backend.dto.chat.ChatResponseDTO;
import lk.ijse.pharmacy_backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponseDTO> chat(@Valid @RequestBody ChatRequestDTO request) {
        ChatResponseDTO response = chatService.processMessage(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<ChatResponseDTO> getStatus() {
        return ResponseEntity.ok(chatService.getStatus());
    }
}
