package org.flbohn.conversationsimulator_backend.conversation.dto.message;

import org.flbohn.conversationsimulator_backend.conversation.domain.Message;
import org.flbohn.conversationsimulator_backend.conversation.types.ConversationMember;

public record MessageResponseDTO(
        long conversationId,
        String message,
        ConversationMember conversationMember
        //long conversationOfMessageId
) {

    public static MessageResponseDTO from(Message message) {
        return new MessageResponseDTO(
                message.getId(),
                message.getMessage(),
                message.getConversationMember()
                // message.getConversationOfMessage().getId()
        );
    }
}
