package org.flbohn.conversationsimulator_backend.evaluation.service;

import org.flbohn.conversationsimulator_backend.evaluation.dto.EvaluationResponseDTO;
import org.flbohn.conversationsimulator_backend.evaluation.dto.MistakeResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service to evaluate Conversations
 */
@Service
public interface EvaluationService {

    /**
     * Provides evaluation at the end of a conversation
     */
    EvaluationResponseDTO receiveEvaluationFromConversation(Long conversationId);

    /**
     * Checks mistakes for new message in the highscore game
     */
    List<MistakeResponseDTO> receiveMistakesForHighscoreMessage(Long conversationId);


}
