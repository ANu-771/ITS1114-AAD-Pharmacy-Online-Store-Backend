package lk.ijse.pharmacy_backend.service;

import lk.ijse.pharmacy_backend.dto.chat.ChatRequestDTO;
import lk.ijse.pharmacy_backend.dto.chat.ChatResponseDTO;

public interface ChatService {

    ChatResponseDTO processMessage(ChatRequestDTO request);

    ChatResponseDTO getStatus();
}
