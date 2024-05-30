package org.flbohn.conversationsimulator_backend.conversation.service;

import org.flbohn.conversationsimulator_backend.conversation.domain.Conversation;
import org.flbohn.conversationsimulator_backend.conversation.domain.Message;
import org.flbohn.conversationsimulator_backend.conversation.repository.ConversationRepository;
import org.flbohn.conversationsimulator_backend.conversation.repository.MessageRepository;
import org.flbohn.conversationsimulator_backend.conversation.types.ConversationMember;
import org.flbohn.conversationsimulator_backend.conversation.types.ConversationStatus;
import org.flbohn.conversationsimulator_backend.exercise.domain.Exercise;
import org.flbohn.conversationsimulator_backend.exercise.domain.Task;
import org.flbohn.conversationsimulator_backend.exercise.service.ExerciseService;
import org.flbohn.conversationsimulator_backend.learner.service.LearnerService;
import org.flbohn.conversationsimulator_backend.llmservices.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ConversationServiceImpl implements ConversationService {

    private static final Logger log = LoggerFactory.getLogger(ConversationServiceImpl.class);
    private final MessageRepository messageRepository;

    private final ConversationRepository conversationRepository;

    private final ExerciseService exerciseService;

    private final OpenAiService openAiService;

    private final LearnerService learnerService;

    @Autowired
    public ConversationServiceImpl(MessageRepository messageRepository, ConversationRepository conversationRepository, ExerciseService exerciseService, OpenAiService openAiService, LearnerService learnerService) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.exerciseService = exerciseService;
        this.openAiService = openAiService;
        this.learnerService = learnerService;
    }

    @Override
    public Message initConversation(Long conversationId) {
        Optional<Conversation> conversationOptional = conversationRepository.findById(conversationId);
        Message partnerMessage = null;

        if (conversationOptional.isPresent()) {
            Conversation conversation = conversationOptional.get();
            partnerMessage = new Message(
                    conversation.isHighscoreConversation()
                            ? openAiService.initHighscoreConversation(conversation)
                            : openAiService.initConversation(conversation.getExercise(), conversation),
                    ConversationMember.PARTNER
            );
            partnerMessage.setConversationOfMessage(conversation);
            conversation.getMessagesOfConversation().add(partnerMessage);
        }
        conversationOptional.orElseThrow();

        return messageRepository.save(partnerMessage);
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

            partnerMessage = new Message(
                    conversation.isHighscoreConversation()
                            ? openAiService.sendHighscoreMessage(conversation.getMessagesOfConversation(), conversation)
                            : openAiService.sendMessage(conversation.getMessagesOfConversation(), conversation.getExercise(), conversation),
                    ConversationMember.PARTNER
            );

            if (!conversation.isHighscoreConversation()) {
                calcEvaluatedTasks(conversation.getMessagesOfConversation(), conversation.getExercise(), conversation);
            }

            partnerMessage.setConversationOfMessage(conversation);
            conversation.getMessagesOfConversation().add(partnerMessage);
        }
        conversationOptional.orElseThrow();


        messageRepository.save(userMessage);
        return messageRepository.save(partnerMessage);
    }

    @Override
    public List<Task> getEvaluatedTasks(Long conversationId) {
        Optional<Conversation> conversationOptional = conversationRepository.findById(conversationId);
        if (conversationOptional.isPresent()) {
            Conversation conversation = conversationOptional.get();
            return calcEvaluatedTasks(conversation.getMessagesOfConversation(), conversation.getExercise(), conversation);
        }
        return new ArrayList<>();
    }

    @Override
    public boolean changeConversationStatus(Long conversationId, ConversationStatus status) {
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (conversationOpt.isPresent()) {
            Conversation conversation = conversationOpt.get();
            conversation.setConversationStatus(status);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Conversation getConversationById(Long conversationId) {
        return conversationRepository.findById(conversationId).orElseThrow();
    }

    @Override
    public Conversation saveConversation(Conversation conversation) {
        return conversationRepository.save(conversation);
    }


    private List<Task> calcEvaluatedTasks(List<Message> allMessages, Exercise exercise, Conversation conversation) {
        String evaluatedTaskMessage = openAiService.evaluateTasksInConversation(allMessages, exercise);
        log.debug("Das sind die bis jetzt erledigten Tasks: {}", evaluatedTaskMessage);

        List<String> evaluatedTasksList = Arrays.asList(evaluatedTaskMessage.split(","));

        evaluatedTasksList.forEach(evaluatedTask -> exercise.getTasks().stream()
                .filter(task -> task.getDescription().equals(evaluatedTask.trim()))
                .findFirst()
                .ifPresent(task -> {
                    if (!conversation.getCompletedTasks().contains(task)) {
                        conversation.getCompletedTasks().add(task);
                        task.getConversationsWhereTaskCompleted().add(conversation);
                    }
                }));

        return conversation.getCompletedTasks();
    }


    @Override
    public Conversation createConversation(Date conversationStartDate, Long exerciseId, Long learnerId) {
        Conversation conversation = new Conversation(conversationStartDate);
        conversation.setExercise(exerciseService.getExerciseById(exerciseId));
        conversation.setLearner(learnerService.findLearnerById(learnerId));
        learnerService.setConversationForLearner(conversation);
        exerciseService.setConversationInExercise(exerciseId, conversation);
        conversation.setRoleSystem(conversation.getExercise().getRoleSystem());
        conversation.setRoleUser(conversation.getExercise().getRoleUser());
        return conversationRepository.save(conversation);
    }

    @Override
    public Conversation createHighScoreConversation(Date conversationStartDate, Long learnerId) {
        Conversation conversation = new Conversation(conversationStartDate);
        conversation.setLearner(learnerService.findLearnerById(learnerId));
        conversation.setSzenario(openAiService.createSzenario());
        String[] roles = openAiService.decideRole(conversation.getSzenario());
        conversation.setRoleUser(roles[0]);
        conversation.setRoleSystem(roles[1]);
        conversation.setHighscoreConversation(true);
        learnerService.setConversationForLearner(conversation);
        return conversationRepository.save(conversation);
    }

    @Override
    public List<Conversation> getAllConversations() {
        return conversationRepository.findAll();
    }

    public void deleteConversation(Long conversationId) {
        conversationRepository.deleteById(conversationId);
    }

}
