package org.flbohn.conversationsimulator_backend.conversation.service;

import org.flbohn.conversationsimulator_backend.conversation.domain.Conversation;
import org.flbohn.conversationsimulator_backend.conversation.domain.Message;
import org.flbohn.conversationsimulator_backend.conversation.types.ConversationMember;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public interface ConversationService {

    Message createMessage(String message, ConversationMember conversationMember, Long conversationId);

    Conversation createConversation(Date conversationStartDate, Long conversationId);

    public Message initConversation(Long conversationId);


}
