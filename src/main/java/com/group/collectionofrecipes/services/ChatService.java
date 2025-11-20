package com.group.collectionofrecipes.services;

import com.group.collectionofrecipes.dto.chatdto.ChatMessageDTO;
import com.group.collectionofrecipes.dto.chatdto.SendMessageDTO;
import com.group.collectionofrecipes.entities.ChatMessage;
import com.group.collectionofrecipes.entities.User;
import com.group.collectionofrecipes.mappers.MessageMapper;
import com.group.collectionofrecipes.repositories.ChatMessageRepository;
import com.group.collectionofrecipes.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final LocalFileStorageService fileStorageService;
    private final MessageMapper messageMapper;

    @Transactional
    public ChatMessageDTO   sendMessage(String senderEmail, SendMessageDTO messageDTO) {
        User sender = userRepository.findByUsername(senderEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Sender not found"));

        ChatMessage message = ChatMessage.builder()
                .content(messageDTO.getContent())
                .sender(sender)
                .attachmentUrl(messageDTO.getAttachmentUrl())
                .isRead(false)
                .build();

        if (messageDTO.getRecipientId() != null) {
            // Личное
            User recipient = userRepository.findById(messageDTO.getRecipientId())
                    .orElseThrow(() -> new RuntimeException("Recipient not found"));
            message.setRecipient(recipient);
            
            ChatMessage savedMsg = chatMessageRepository.save(message);
            ChatMessageDTO responseDTO = messageMapper.toChatMessageDTO(savedMsg);

            messagingTemplate.convertAndSendToUser(
                    recipient.getEmail(), 
                    "/queue/messages", 
                    responseDTO
            );
             messagingTemplate.convertAndSendToUser(
                    sender.getEmail(), 
                    "/queue/messages", 
                    responseDTO
            );
            return responseDTO;

        } else {
            // Общее
            message.setRecipient(null);
            ChatMessage savedMsg = chatMessageRepository.save(message);
            ChatMessageDTO responseDTO = messageMapper.toChatMessageDTO(savedMsg);

            messagingTemplate.convertAndSend("/topic/public", responseDTO);
            return responseDTO;
        }
    }

    public String uploadFile(MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);
        return "/uploads/" + fileName;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getPublicHistory(Long fromId) {
        List<ChatMessage> messages;
        if (fromId == null || fromId == 0) {
             messages = chatMessageRepository.findLastPublicMessages();
        } else {
             messages = chatMessageRepository.findPublicMessagesAfterId(fromId);
        }

        List<ChatMessageDTO> messageDTOS = new ArrayList<>();
        for (ChatMessage message : messages) {
            messageDTOS.add(messageMapper.toChatMessageDTO(message));
        }
        return messageDTOS;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getPrivateHistory(String currentUserEmail, Long otherUserId) {
        User currentUser = userRepository.findByUsername(currentUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<ChatMessage> messages = chatMessageRepository.findConversation(currentUser.getId(), otherUserId);

        List<ChatMessageDTO> messageDTOS = new ArrayList<>();
        for (ChatMessage message : messages) {
            messageDTOS.add(messageMapper.toChatMessageDTO(message));
        }
        return messageDTOS;
    }
}
