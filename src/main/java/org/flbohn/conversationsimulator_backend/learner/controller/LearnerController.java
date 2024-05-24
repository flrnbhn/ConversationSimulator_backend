package org.flbohn.conversationsimulator_backend.learner.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.flbohn.conversationsimulator_backend.conversation.controller.ConversationController;
import org.flbohn.conversationsimulator_backend.learner.dto.LearnerLoginRequestDTO;
import org.flbohn.conversationsimulator_backend.learner.dto.LearnerRegistrateRequestDTO;
import org.flbohn.conversationsimulator_backend.learner.service.LearnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
