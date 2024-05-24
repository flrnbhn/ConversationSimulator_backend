package org.flbohn.conversationsimulator_backend.learner.service;

import org.flbohn.conversationsimulator_backend.conversation.domain.Conversation;
import org.flbohn.conversationsimulator_backend.learner.domain.Learner;
import org.springframework.stereotype.Service;

@Service
public interface LearnerService {

    long registrateLearner(String name, String learningLanguage);

    long loginLearner(String name);

    Learner findLearnerById(long id);

    void setConversationForLearner(Conversation conversation);
}
