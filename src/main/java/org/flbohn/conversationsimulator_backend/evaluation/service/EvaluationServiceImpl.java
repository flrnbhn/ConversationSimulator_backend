package org.flbohn.conversationsimulator_backend.evaluation.service;

import org.flbohn.conversationsimulator_backend.conversation.domain.Conversation;
import org.flbohn.conversationsimulator_backend.conversation.domain.Message;
import org.flbohn.conversationsimulator_backend.conversation.service.ConversationService;
import org.flbohn.conversationsimulator_backend.conversation.types.ConversationMember;
import org.flbohn.conversationsimulator_backend.conversation.types.Grade;
import org.flbohn.conversationsimulator_backend.evaluation.domain.Mistake;
import org.flbohn.conversationsimulator_backend.evaluation.dto.EvaluationResponseDTO;
import org.flbohn.conversationsimulator_backend.evaluation.dto.MistakeResponseDTO;
import org.flbohn.conversationsimulator_backend.evaluation.repository.MistakeRepository;
import org.flbohn.conversationsimulator_backend.llmservices.LanguageCheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class EvaluationServiceImpl implements EvaluationService {

    private static final Logger log = LoggerFactory.getLogger(EvaluationServiceImpl.class);

    private final LanguageCheckService languageCheckService;

    private final ConversationService conversationService;

    private final MistakeRepository mistakeRepository;


    @Autowired
    public EvaluationServiceImpl(LanguageCheckService languageCheckService, ConversationService conversationService, MistakeRepository mistakeRepository) {
        this.languageCheckService = languageCheckService;
        this.conversationService = conversationService;
        this.mistakeRepository = mistakeRepository;
    }

    @Override
    public String checkLanguage(String language) {
        String s = languageCheckService.checkLanguage(language).block();
        log.warn(s);
        return s;
    }

    public List<MistakeResponseDTO> receiveMistakes(String conversation) {
        List<MistakeResponseDTO> mistakeResponseDTOS = languageCheckService.checkConversation(conversation).block();
        //persistieren
        return mistakeResponseDTOS;
    }

    @Override
    public EvaluationResponseDTO receiveMistakesByConversation(Long conversationId) {
        List<MistakeResponseDTO> allMistakeResponseDTOS = new ArrayList<>();
        Conversation conversation = conversationService.getConversationById(conversationId);
        List<Message> messages = conversation.getMessagesOfConversation().stream()
                .filter(message -> message.getConversationMember() == ConversationMember.USER)
                .toList();

        for (Message message : messages) {
            List<MistakeResponseDTO> currentMistakeResponseDTOS = Objects.requireNonNull(languageCheckService.checkConversation(message.getMessage()).block());
            persistMistakesInConversationAndMessage(currentMistakeResponseDTOS, conversation, message);
            allMistakeResponseDTOS.addAll(currentMistakeResponseDTOS);
        }

        Grade grade = assignGradeToConversation(conversation, allMistakeResponseDTOS);
        Integer points = assignPointsToConversation(conversation, grade);
        conversationService.saveConversation(conversation);

        return new EvaluationResponseDTO(allMistakeResponseDTOS, grade, points);
    }

    private void persistMistakesInConversationAndMessage(List<MistakeResponseDTO> mistakeResponseDTOS, Conversation conversation, Message message) {
        if (mistakeResponseDTOS != null) {
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

    private Grade assignGradeToConversation(Conversation conversation, List<MistakeResponseDTO> mistakeResponseDTOS) {
        Grade grade = calculateGrade(mistakeResponseDTOS);
        conversation.setGradeOfConversation(grade);
        conversation.getLearner().getAllGrades().add(grade);
        return grade;
    }

    private Grade calculateGrade(List<MistakeResponseDTO> mistakeResponseDTOS) {
        int mistakeCount = mistakeResponseDTOS.size();
        return switch (mistakeCount) {
            case 0, 1 -> Grade.ONE;
            case 2, 3, 4 -> Grade.TWO;
            case 5, 6, 7 -> Grade.THREE;
            case 8, 9, 10 -> Grade.FOUR;
            case 11, 12, 13 -> Grade.FIVE;
            default -> Grade.SIX;
        };
    }

    private int assignPointsToConversation(Conversation conversation, Grade grade) {
        int points = (int) (17 - 3 * grade.getNumericValue());
        conversation.setPointsOfConversation(points);
        conversation.getLearner().setTotalPoints(conversation.getLearner().getTotalPoints() + points);
        return points;
    }

}
