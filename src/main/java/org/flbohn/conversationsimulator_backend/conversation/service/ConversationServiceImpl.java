package org.flbohn.conversationsimulator_backend.conversation.service;

import org.flbohn.conversationsimulator_backend.conversation.domain.Conversation;
import org.flbohn.conversationsimulator_backend.conversation.domain.Message;
import org.flbohn.conversationsimulator_backend.conversation.repository.ConversationRepository;
import org.flbohn.conversationsimulator_backend.conversation.repository.MessageRepository;
import org.flbohn.conversationsimulator_backend.conversation.types.ConversationMember;
import org.flbohn.conversationsimulator_backend.exercise.service.ExerciseService;
import org.flbohn.conversationsimulator_backend.llmservices.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ConversationServiceImpl implements ConversationService {

    private final MessageRepository messageRepository;

    private final ConversationRepository conversationRepository;

    private final ExerciseService exerciseService;

    private final OpenAiService openAiService;

    @Autowired
    public ConversationServiceImpl(MessageRepository messageRepository, ConversationRepository conversationRepository, ExerciseService exerciseService, OpenAiService openAiService) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.exerciseService = exerciseService;
        this.openAiService = openAiService;
    }

    @Override
    public Message createMessage(String messageString, ConversationMember conversationMember, Long conversationId) throws NoSuchElementException {
        final Message userMessage = new Message(messageString, conversationMember);
        Message partnerMessage = null;
        Optional<Conversation> conversationOptional = conversationRepository.findById(conversationId);

        if (conversationOptional.isPresent()) {
            Conversation conversation = conversationOptional.get();
            userMessage.setConversationOfMessage(conversation);
            conversation.getMessagesOfConversation().add(userMessage);

            partnerMessage = new Message(openAiService.sendMessage(conversation.getMessagesOfConversation(), conversation.getExercise()), ConversationMember.PARTNER);

            partnerMessage.setConversationOfMessage(conversation);
            conversation.getMessagesOfConversation().add(partnerMessage);
        }
        conversationOptional.orElseThrow();

        messageRepository.save(userMessage);
        return messageRepository.save(partnerMessage);
    }

    public Message initConversation(Long conversationId) {
        Optional<Conversation> conversationOptional = conversationRepository.findById(conversationId);
        Message partnerMessage = null;

        if (conversationOptional.isPresent()) {
            Conversation conversation = conversationOptional.get();
            partnerMessage = new Message(openAiService.initConversation(conversation.getExercise()), ConversationMember.PARTNER);
            partnerMessage.setConversationOfMessage(conversation);
            conversation.getMessagesOfConversation().add(partnerMessage);
        }
        conversationOptional.orElseThrow();

        return messageRepository.save(partnerMessage);
    }


    @Override
    public Conversation createConversation(Date conversationStartDate, Long exerciseId) {
        Conversation conversation = new Conversation(conversationStartDate);
        conversation.setExercise(exerciseService.getExerciseById(exerciseId));
        exerciseService.setConversationInExercise(exerciseId, conversation);
        return conversationRepository.save(conversation);
    }
}
