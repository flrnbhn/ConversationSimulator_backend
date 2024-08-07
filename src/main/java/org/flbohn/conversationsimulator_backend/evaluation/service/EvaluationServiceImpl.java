package org.flbohn.conversationsimulator_backend.evaluation.service;

import org.flbohn.conversationsimulator_backend.conversation.domain.Conversation;
import org.flbohn.conversationsimulator_backend.conversation.domain.Message;
import org.flbohn.conversationsimulator_backend.conversation.service.ConversationService;
import org.flbohn.conversationsimulator_backend.conversation.types.ConversationMember;
import org.flbohn.conversationsimulator_backend.conversation.types.ConversationStatus;
import org.flbohn.conversationsimulator_backend.conversation.types.Grade;
import org.flbohn.conversationsimulator_backend.evaluation.domain.Mistake;
import org.flbohn.conversationsimulator_backend.evaluation.dto.EvaluationResponseDTO;
import org.flbohn.conversationsimulator_backend.evaluation.dto.MistakeResponseDTO;
import org.flbohn.conversationsimulator_backend.evaluation.repository.MistakeRepository;
import org.flbohn.conversationsimulator_backend.learner.domain.Learner;
import org.flbohn.conversationsimulator_backend.otherservices.LLMService;
import org.flbohn.conversationsimulator_backend.otherservices.LanguageCheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class EvaluationServiceImpl implements EvaluationService {

    private static final Logger log = LoggerFactory.getLogger(EvaluationServiceImpl.class);

    private final LanguageCheckService languageCheckService;

    private final ConversationService conversationService;

    private final MistakeRepository mistakeRepository;

    private final LLMService LLMService;


    @Autowired
    public EvaluationServiceImpl(LanguageCheckService languageCheckService, ConversationService conversationService, MistakeRepository mistakeRepository, LLMService LLMService) {
        this.languageCheckService = languageCheckService;
        this.conversationService = conversationService;
        this.mistakeRepository = mistakeRepository;
        this.LLMService = LLMService;
    }

    @Override
    @Transactional
    public EvaluationResponseDTO receiveEvaluationFromConversation(Long conversationId) {
        List<MistakeResponseDTO> allMistakeResponseDTOS = new ArrayList<>();
        Conversation conversation = conversationService.getConversationById(conversationId);
        List<Message> messages = conversation.getMessagesOfConversation().stream()
                .filter(message -> message.getConversationMember() == ConversationMember.USER)
                .toList();

        List<MistakeResponseDTO> currentMistakeResponseDTOS;
        for (Message message : messages) {
            if (message.isVoiceMessage()) {
                currentMistakeResponseDTOS = Objects.requireNonNull(languageCheckService.checkConversationMessage_audio(message.getMessage())).block();
            } else {
                currentMistakeResponseDTOS = Objects.requireNonNull(languageCheckService.checkConversationMessage_text(message.getMessage())).block();
            }
            persistMistakesInConversationAndMessage(currentMistakeResponseDTOS, conversation, message);
            allMistakeResponseDTOS.addAll(currentMistakeResponseDTOS);
        }
        String evaluation = LLMService.evaluateConversation(conversation.getMessagesOfConversation(), conversation.getExercise(), conversation);

        Grade grade = assignGradeToConversation(conversation, allMistakeResponseDTOS, messages);
        Integer points = assignPointsToConversation(conversation, grade);
        conversationService.saveConversation(conversation);

        return new EvaluationResponseDTO(allMistakeResponseDTOS, grade, points, evaluation, conversation.getTranslationCount(), conversation.getMessagesOfConversation().size() - 1);
    }


    @Override
    public List<MistakeResponseDTO> receiveMistakesForHighscoreMessage(Long conversationId) {
        Conversation conversation = conversationService.getConversationById(conversationId);
        Message message = conversation.getMessagesOfConversation().get(conversation.getMessagesOfConversation().size() - 2);
        List<MistakeResponseDTO> mistakeResponseDTOs;
        if (message.isVoiceMessage()) {
            mistakeResponseDTOs = Objects.requireNonNull(languageCheckService.checkConversationMessage_audio(message.getMessage()).block());
        } else {
            mistakeResponseDTOs = Objects.requireNonNull(languageCheckService.checkConversationMessage_text(message.getMessage()).block());
        }
        persistMistakesInConversationAndMessage(mistakeResponseDTOs, conversation, message);

        return mistakeResponseDTOs;
    }

    private void persistMistakesInConversationAndMessage(List<MistakeResponseDTO> mistakeResponseDTOS, Conversation conversation, Message message) {
        if (mistakeResponseDTOS != null && !mistakeResponseDTOS.isEmpty()) {
            List<Mistake> mistakes = mistakeResponseDTOS.stream().map(mistakeResponseDTO ->
                            new Mistake(mistakeResponseDTO.message(),
                                    mistakeResponseDTO.shortMessage(),
                                    mistakeResponseDTO.replacements(),
                                    mistakeResponseDTO.offset(),
                                    mistakeResponseDTO.length(),
                                    mistakeResponseDTO.sentence()))
                    .toList();
            conversation.getMistakes().addAll(mistakes);
            message.getMistakes().addAll(mistakes);
            mistakes.forEach(mistake -> {
                mistake.setConversationOfTheMistake(conversation);
                mistake.setMessageOfTheMistake(message);
            });
            mistakeRepository.saveAll(mistakes);
        }
    }

    private Grade assignGradeToConversation(Conversation conversation, List<MistakeResponseDTO> mistakeResponseDTOS, List<Message> messages) {
        Grade grade = calculateGrade(mistakeResponseDTOS, messages, conversation);
        if (conversation.getConversationStatus() == ConversationStatus.PASSED) {
            conversation.setGradeOfConversation(grade);
            conversation.getLearner().getAllGrades().add(grade);
            conversation.getLearner().setGradeAverage(calcGradeAverage(conversation.getLearner()));
        }
        return grade;
    }

    private float calcGradeAverage(Learner learner) {
        List<Grade> allGrades = learner.getAllGrades();
        if (allGrades.isEmpty()) {
            return 0.0F;
        }
        float gradeAddition = 0.0F;
        for (Grade grade : allGrades) {
            gradeAddition += (float) grade.getNumericValue();
        }

        return gradeAddition / allGrades.size();
    }

    private Grade calculateGrade(List<MistakeResponseDTO> mistakeResponseDTOS, List<Message> messages, Conversation conversation) {
        int mistakeCount = mistakeResponseDTOS.size();
        int wordCount = countWordsInUserMessages(messages.stream().map(Message::getMessage).toList());
        if (mistakeCount == 0) {
            return Grade.ONE;
        }

        double failureRate = ((double) mistakeCount / wordCount) * 100;
        failureRate -= ((double) conversation.getTranslationCount() / 10);

        if (failureRate <= 1) {
            return Grade.ONE;
        } else if (failureRate <= 3) {
            return Grade.TWO;
        } else if (failureRate <= 6) {
            return Grade.THREE;
        } else if (failureRate <= 9) {
            return Grade.FOUR;
        } else if (failureRate <= 12) {
            return Grade.FIVE;
        } else {
            return Grade.SIX;
        }
    }

    private int assignPointsToConversation(Conversation conversation, Grade grade) {
        int points;
        if (conversation.getConversationStatus() == ConversationStatus.FAILED) {
            points = -5;
        } else {
            points = (int) (18 - 3 * grade.getNumericValue());
        }
        conversation.setPointsOfConversation(points);
        conversation.getLearner().setTotalPoints(conversation.getLearner().getTotalPoints() + points);
        return points;
    }

    private Integer countWordsInUserMessages(List<String> messages) {
        int count = 0;
        for (String message : messages) {
            count += countWordsInString(message);
        }
        return count;
    }

    private Integer countWordsInString(String text) {
        String[] words = text.split("\\s+|\\n+");
        return words.length;
    }

}
