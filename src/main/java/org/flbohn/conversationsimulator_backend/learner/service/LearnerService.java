package org.flbohn.conversationsimulator_backend.learner.service;

import org.flbohn.conversationsimulator_backend.conversation.domain.Conversation;
import org.flbohn.conversationsimulator_backend.learner.domain.Learner;
import org.flbohn.conversationsimulator_backend.learner.dto.HighScoreLearnersResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface LearnerService {

    long registrateLearner(String name, String learningLanguage);

    long loginLearner(String name);

    Learner findLearnerById(long id);

    List<Learner> findAllLearners();

    void setConversationForLearner(Conversation conversation);

    List<Conversation> getAllConversationsFromLearner(long id);

    List<HighScoreLearnersResponseDTO> getAllHighscores();
}
