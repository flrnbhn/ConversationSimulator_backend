package org.flbohn.conversationsimulator_backend.learner.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.flbohn.conversationsimulator_backend.conversation.controller.ConversationController;
import org.flbohn.conversationsimulator_backend.conversation.domain.Conversation;
import org.flbohn.conversationsimulator_backend.conversation.dto.conversation.ConversationResponseDTO;
import org.flbohn.conversationsimulator_backend.learner.domain.Learner;
import org.flbohn.conversationsimulator_backend.learner.dto.HighScoreLearnersResponseDTO;
import org.flbohn.conversationsimulator_backend.learner.dto.LearnerLoginRequestDTO;
import org.flbohn.conversationsimulator_backend.learner.dto.LearnerRegistrateRequestDTO;
import org.flbohn.conversationsimulator_backend.learner.dto.LearnerResponseDTO;
import org.flbohn.conversationsimulator_backend.learner.service.LearnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/learner")
public class LearnerController {

    private static final Logger log = LoggerFactory.getLogger(ConversationController.class);
    private final LearnerService learnerService;

    @Autowired
    public LearnerController(LearnerService learnerService) {
        this.learnerService = learnerService;
    }

    @Operation(summary = "Registrate Learner")
    @PostMapping("/registrate")
    public ResponseEntity<Long> registrateLearner(@RequestBody LearnerRegistrateRequestDTO learnerRegistrateRequestDTO) {
        long id = learnerService.registrateLearner(learnerRegistrateRequestDTO.name(), learnerRegistrateRequestDTO.learningLanguage());
        if (id != -1) {
            return ResponseEntity.ok(id);
        }
        return ResponseEntity.badRequest().build();
    }

    @Operation(summary = "Login Learner")
    @PostMapping("/login")
    public ResponseEntity<Long> loginLearner(@RequestBody LearnerLoginRequestDTO learnerLoginRequestDTO) {
        long id = learnerService.loginLearner(learnerLoginRequestDTO.name());
        if (id != -1) {
            return ResponseEntity.ok(id);
        }
        return ResponseEntity.badRequest().build();
    }

    @Operation(summary = "Get Learner by Id")
    @GetMapping("/{learnderId}")
    public ResponseEntity<LearnerResponseDTO> getLearnerById(@PathVariable long learnderId) {
        return new ResponseEntity<>(LearnerResponseDTO.from(learnerService.findLearnerById(learnderId)), HttpStatus.OK);
    }

    @Operation(summary = "Get all  sorted by total Points")
    @GetMapping("")
    public ResponseEntity<List<LearnerResponseDTO>> getAllLearner() {
        List<Learner> learners = learnerService.findAllLearners();
        return new ResponseEntity<>(learners.stream()
                .map(LearnerResponseDTO::from)
                .sorted(Comparator.comparingInt(LearnerResponseDTO::totalPoints))
                .toList(), HttpStatus.OK);
    }

    @Operation(summary = "Get all Conversation from Learner")
    @GetMapping("conversations/{learnerId}")
    public ResponseEntity<List<ConversationResponseDTO>> getConversationsByLearnerId(@PathVariable long learnerId) {
        List<Conversation> conversations = learnerService.getAllConversationsFromLearner(learnerId);
        return new ResponseEntity<>(conversations.stream()
                .map(ConversationResponseDTO::from)
                .toList(), HttpStatus.OK);
    }

    @Operation(summary = "Get all HighScores from all Learners")
    @GetMapping("/highscore")
    public ResponseEntity<List<HighScoreLearnersResponseDTO>> getAllHighscoresFromAllLearners() {
        return new ResponseEntity<>(learnerService.getAllHighscores(), HttpStatus.OK);
    }
}
