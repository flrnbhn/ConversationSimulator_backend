package org.flbohn.conversationsimulator_backend.learner.service;

import org.flbohn.conversationsimulator_backend.conversation.domain.Conversation;
import org.flbohn.conversationsimulator_backend.conversation.repository.ConversationRepository;
import org.flbohn.conversationsimulator_backend.conversation.types.ConversationStatus;
import org.flbohn.conversationsimulator_backend.learner.domain.Learner;
import org.flbohn.conversationsimulator_backend.learner.dto.HighScoreLearnersResponseDTO;
import org.flbohn.conversationsimulator_backend.learner.repository.LearnerRepository;
import org.flbohn.conversationsimulator_backend.learner.types.LearningLanguage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LearnerServiceImpl implements LearnerService {

    private final LearnerRepository learnerRepository;
    private final ConversationRepository conversationRepository;

    @Autowired
    public LearnerServiceImpl(LearnerRepository learnerRepository, ConversationRepository conversationRepository) {
        this.learnerRepository = learnerRepository;
        this.conversationRepository = conversationRepository;
    }

    @Override
    public long registrateLearner(String name, LearningLanguage learningLanguage) {
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

    public List<Learner> findAllLearners() {
        return learnerRepository.findAll();
    }

    @Override
    public void setConversationForLearner(Conversation conversation) {
        conversation.getLearner().getConversations().add(conversation);
    }

    @Override
    public List<Conversation> getAllConversationsFromLearner(long id) {
        return learnerRepository.findById(id).orElseThrow().getConversations().stream()
                .filter(conversation -> !conversation.isHighscoreConversation())
                .filter(conversation -> conversation.getConversationStatus() == ConversationStatus.PASSED)
                .toList();
    }

    @Override
    public List<HighScoreLearnersResponseDTO> getAllHighscores() {
        List<Conversation> allConversations = conversationRepository.findAll();
        return allConversations.stream().
                filter(Conversation::isHighscoreConversation).
                map(HighScoreLearnersResponseDTO::from).
                toList();
    }

    @Override
    public void changeLearningLanguage(long id, LearningLanguage learningLanguage) {
        Learner learner = learnerRepository.findById(id).orElseThrow();
        learner.setLearningLanguage(learningLanguage);
        learnerRepository.save(learner);
    }
}
