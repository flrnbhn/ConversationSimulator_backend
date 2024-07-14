package org.flbohn.conversationsimulator_backend.learner.service;

import org.flbohn.conversationsimulator_backend.conversation.domain.Conversation;
import org.flbohn.conversationsimulator_backend.learner.domain.Learner;
import org.flbohn.conversationsimulator_backend.learner.dto.HighScoreLearnersResponseDTO;
import org.flbohn.conversationsimulator_backend.learner.types.LearningLanguage;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for user management
 */
@Service
public interface LearnerService {

    long registrateLearner(String name, LearningLanguage learningLanguage);

    long loginLearner(String name);

    Learner findLearnerById(long id);

    List<Learner> findAllLearners();

    void setConversationForLearner(Conversation conversation);

    /**
     * Get all finished conversation from learner. This includes also grades and points for each conversation
     */
    List<Conversation> getAllFinishedConversationsFromLearner(long id);

    /**
     * Get all highscores from all Learners
     */
    List<HighScoreLearnersResponseDTO> getAllHighscores();

    void changeLearningLanguage(long id, LearningLanguage learningLanguage);

}
