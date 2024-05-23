package org.flbohn.conversationsimulator_backend.evaluation.service;

import org.flbohn.conversationsimulator_backend.evaluation.dto.MistakeResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EvaluationService {

    String checkLanguage(String language);

    List<MistakeResponseDTO> receiveMistakes(String conversation);

    List<MistakeResponseDTO> receiveMistakesByConversation(Long conversationId);

}
