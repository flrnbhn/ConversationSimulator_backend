package org.flbohn.conversationsimulator_backend.conversation.service;

import org.flbohn.conversationsimulator_backend.conversation.domain.Conversation;
import org.flbohn.conversationsimulator_backend.conversation.domain.Message;
import org.flbohn.conversationsimulator_backend.conversation.types.ConversationMember;
import org.flbohn.conversationsimulator_backend.conversation.types.ConversationStatus;
import org.flbohn.conversationsimulator_backend.exercise.domain.Task;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public interface ConversationService {

    Message createMessage(String message, ConversationMember conversationMember, Long conversationId);

    Conversation createConversation(Date conversationStartDate, Long conversationId);

    Message initConversation(Long conversationId);

    List<Task> getEvaluatedTasks(Long conversationId);

    boolean changeConversationStatus(Long conversationId, ConversationStatus status);


}
