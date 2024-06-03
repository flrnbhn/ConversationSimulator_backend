package org.flbohn.conversationsimulator_backend.conversation.dto.message;

import org.flbohn.conversationsimulator_backend.conversation.domain.Message;
import org.flbohn.conversationsimulator_backend.conversation.types.ConversationMember;

public record MessageResponseDTO(
        long conversationId,
        String message,
        ConversationMember conversationMember,
        String synthesizedMessage
        //long conversationOfMessageId
) {

    public static MessageResponseDTO from(Message message, String synthesizedMessage) {
        return new MessageResponseDTO(
                message.getId(),
                message.getMessage(),
                message.getConversationMember(),
                synthesizedMessage
                // message.getConversationOfMessage().getId()
        );
    }
}
