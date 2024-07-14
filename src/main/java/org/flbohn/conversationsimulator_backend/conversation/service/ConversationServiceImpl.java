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
import org.flbohn.conversationsimulator_backend.otherservices.LLMService;
import org.flbohn.conversationsimulator_backend.otherservices.Speech2TextService;
import org.flbohn.conversationsimulator_backend.otherservices.Text2SpeechService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ConversationServiceImpl implements ConversationService {

    private static final Logger log = LoggerFactory.getLogger(ConversationServiceImpl.class);
    private final MessageRepository messageRepository;

    private final ConversationRepository conversationRepository;

    private final ExerciseService exerciseService;

    private final LLMService LLMService;

    private final LearnerService learnerService;

    private final Text2SpeechService text2SpeechService;

    private final Speech2TextService speech2TextService;


    @Autowired
    public ConversationServiceImpl(MessageRepository messageRepository, ConversationRepository conversationRepository, ExerciseService exerciseService, LLMService LLMService, LearnerService learnerService, Text2SpeechService text2SpeechService, Speech2TextService speech2TextService) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.exerciseService = exerciseService;
        this.LLMService = LLMService;
        this.learnerService = learnerService;
        this.text2SpeechService = text2SpeechService;
        this.speech2TextService = speech2TextService;
    }

    @Override
    @Transactional
    public Message initConversation(Long conversationId) {
        Optional<Conversation> conversationOptional = conversationRepository.findById(conversationId);
        Message partnerMessage = null;

        if (conversationOptional.isPresent()) {
            Conversation conversation = conversationOptional.get();
            partnerMessage = new Message(
                    conversation.isHighscoreConversation()
                            ? LLMService.initHighscoreConversation(conversation)
                            : LLMService.initConversation(conversation.getExercise(), conversation),
                    ConversationMember.PARTNER
            );
            partnerMessage.setConversationOfMessage(conversation);
            conversation.getMessagesOfConversation().add(partnerMessage);
            conversationRepository.save(conversation);
        }
        conversationOptional.orElseThrow();

        return messageRepository.save(partnerMessage);
    }

    @Override
    @Transactional
    public Message createMessage(String messageString, ConversationMember conversationMember, Long conversationId, boolean isAudioMessage) throws NoSuchElementException {
        Message userMessage;
        userMessage = new Message(messageString, conversationMember, isAudioMessage);

        Message partnerMessage = null;
        Optional<Conversation> conversationOptional = conversationRepository.findById(conversationId);

        if (conversationOptional.isPresent()) {
            Conversation conversation = conversationOptional.get();
            userMessage.setConversationOfMessage(conversation);
            conversation.getMessagesOfConversation().add(userMessage);

            partnerMessage = new Message(
                    conversation.isHighscoreConversation()
                            ? LLMService.sendHighscoreMessage(conversation.getMessagesOfConversation(), conversation)
                            : LLMService.generateMessage(conversation.getMessagesOfConversation(), conversation.getExercise(), conversation),
                    ConversationMember.PARTNER
            );

            partnerMessage.setConversationOfMessage(conversation);
            conversation.getMessagesOfConversation().add(partnerMessage);
        }
        conversationOptional.orElseThrow();
        messageRepository.save(userMessage);
        return messageRepository.save(partnerMessage);
    }

    @Override
    public List<Task> getFinishedTasks(Long conversationId) {
        Optional<Conversation> conversationOptional = conversationRepository.findById(conversationId);
        if (conversationOptional.isPresent() && conversationOptional.get().getMessagesOfConversation().size() > 1) {
            Conversation conversation = conversationOptional.get();
            return calcFinishedTasks(conversation.getMessagesOfConversation(), conversation.getExercise(), conversation);
        }
        return new ArrayList<>();
    }

    @Override
    public boolean changeConversationStatus(Long conversationId, ConversationStatus status) {
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (conversationOpt.isPresent()) {
            Conversation conversation = conversationOpt.get();
            conversation.setConversationStatus(status);
            if (conversation.getConversationStatus() != ConversationStatus.IN_PROCESS) {
                conversationRepository.save(conversation);
            }
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
    @Transactional
    public Conversation saveConversation(Conversation conversation) {
        return conversationRepository.save(conversation);
    }


    private List<Task> calcFinishedTasks(List<Message> allMessages, Exercise exercise, Conversation conversation) {
        String evaluatedTaskMessage = LLMService.evaluateTasksInConversation(allMessages, exercise);
        log.debug("Das sind die bis jetzt erledigten Tasks: {}", evaluatedTaskMessage);

        List<String> evaluatedTasksList = Arrays.asList(evaluatedTaskMessage.split(","));

        evaluatedTasksList.forEach(evaluatedTask -> exercise.getTasks().stream()
                .filter(task -> task.getDescription().equals(evaluatedTask.trim()))
                .findFirst()
                .ifPresent(task -> {
                    if (!conversation.getCompletedTasks().contains(task)) {
                        conversation.getCompletedTasks().add(task);
                        task.getConversationsWhereTaskCompleted().add(conversation);
                        conversationRepository.save(conversation);
                    }
                }));

        return conversation.getCompletedTasks();
    }


    @Override
    @Transactional
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
    @Transactional
    public Conversation createHighScoreConversation(Date conversationStartDate, Long learnerId) {
        Conversation conversation = new Conversation(conversationStartDate);
        conversation.setLearner(learnerService.findLearnerById(learnerId));
        conversation.setSzenario(LLMService.createSzenario());
        String[] roles = LLMService.decideRole(conversation.getSzenario());
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

    @Override
    @Transactional
    public String synthesizeMessageFromConversation(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow();
        String lastMessage = conversation.getMessagesOfConversation().stream()
                .filter(message -> message.getConversationMember() == ConversationMember.PARTNER)
                .toList()
                .getLast()
                .getMessage();

        if (conversation.getGender().isEmpty()) {
            conversation.setGender(LLMService.decideGenderByName(conversation.getRoleSystem()));
            conversationRepository.save(conversation);
        }
        byte[] audioBytes = text2SpeechService.synthesizeSpeech(lastMessage, conversation.getLearner().getLearningLanguage().getLanguageValue(), conversation.getGender());

        return Base64.getEncoder().encodeToString(audioBytes);
    }

    public String transcribeMessage(String base64EncodedMessage) {
        return speech2TextService.transcription(base64EncodedMessage);
    }

    @Override
    @Transactional
    public String translateMessage(String message, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow();
        conversation.setTranslationCount(conversation.getTranslationCount() + 1);
        conversationRepository.save(conversation);
        return LLMService.translateMessage(message);
    }


}
