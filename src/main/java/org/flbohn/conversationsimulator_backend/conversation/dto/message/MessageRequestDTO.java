package org.flbohn.conversationsimulator_backend.conversation.dto.message;

import org.flbohn.conversationsimulator_backend.conversation.types.ConversationMember;

public record MessageRequestDTO(
        String message,
        ConversationMember conversationMember,
        Long conversationID,
        boolean isAudioMessage
) {

}
