package org.flbohn.conversationsimulator_backend.learner.service;

import org.flbohn.conversationsimulator_backend.conversation.domain.Conversation;
import org.flbohn.conversationsimulator_backend.learner.domain.Learner;
import org.flbohn.conversationsimulator_backend.learner.repository.LearnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LearnerServiceImpl implements LearnerService {

    private final LearnerRepository learnerRepository;

    @Autowired
    public LearnerServiceImpl(LearnerRepository learnerRepository) {
        this.learnerRepository = learnerRepository;
    }

    @Override
    public long registrateLearner(String name, String learningLanguage) {
        if (learnerRepository.findByName(name).isPresent()) {
            return -1;
        }

        Learner newLearner = new Learner(name, learningLanguage);
        newLearner = learnerRepository.save(newLearner);
        return newLearner.getId();
    }

    @Override
    public long loginLearner(String name) {
        Optional<Learner> learnerOptional = learnerRepository.findByName(name);
        if (learnerOptional.isPresent()) {
            return learnerOptional.get().getId();
        }
        return -1;
    }

    @Override
    public Learner findLearnerById(long id) {
        return learnerRepository.findById(id).orElseThrow();
    }

    @Override
    public void setConversationForLearner(Conversation conversation) {
        conversation.getLearner().setConversation(conversation);
    }
}
