package org.flbohn.conversationsimulator_backend.evaluation.service;

import org.flbohn.conversationsimulator_backend.evaluation.dto.EvaluationResponseDTO;
import org.flbohn.conversationsimulator_backend.evaluation.dto.MistakeResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EvaluationService {

    String checkLanguage(String language);

    List<MistakeResponseDTO> receiveMistakes(String conversation);

    EvaluationResponseDTO receiveMistakesByConversation(Long conversationId);

    List<MistakeResponseDTO> receiveMistakesByConversationInHighscoreGame(Long conversationId);


}
