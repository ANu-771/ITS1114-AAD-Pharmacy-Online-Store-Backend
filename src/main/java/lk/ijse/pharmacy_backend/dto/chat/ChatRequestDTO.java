package lk.ijse.pharmacy_backend.dto.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequestDTO {

    @NotBlank(message = "Message cannot be empty")
    private String message;

    private List<ChatMessageDTO> history;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatMessageDTO {
        private String role; // "user" or "model"
        private String text;
    }
}
