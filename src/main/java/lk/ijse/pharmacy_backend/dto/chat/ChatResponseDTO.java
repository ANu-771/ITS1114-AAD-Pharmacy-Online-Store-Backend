package lk.ijse.pharmacy_backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponseDTO {

    private String reply;
    private String status; // "SUCCESS", "UNAVAILABLE", "ERROR"
    private boolean available;
    private LocalDateTime timestamp;
}
