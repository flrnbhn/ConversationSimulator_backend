package org.flbohn.conversationsimulator_backend.conversation.dto.conversation;

import java.util.Date;

public record ConversationRequestDTO(
        Date conversationStartDate,
        Long exerciseId,
        Long learnerId
) {

}