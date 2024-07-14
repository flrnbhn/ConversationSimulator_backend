package org.flbohn.conversationsimulator_backend.evaluation.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.flbohn.conversationsimulator_backend.evaluation.dto.EvaluationResponseDTO;
import org.flbohn.conversationsimulator_backend.evaluation.dto.MistakeResponseDTO;
import org.flbohn.conversationsimulator_backend.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * restcontroller to evaluate conversation
 */

@RestController
@RequestMapping("/evaluation")
public class EvaluationController {

    private final EvaluationService evaluationService;

    @Autowired
    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }


    @Operation(summary = "Check Conversation about mistakes")
    @PostMapping("/{conversationId}")
    public ResponseEntity<EvaluationResponseDTO> postNewLanguageCheckForConversation(@PathVariable Long conversationId) {
        return new ResponseEntity<>(evaluationService.receiveEvaluationFromConversation(conversationId), HttpStatus.OK);
    }

    @Operation(summary = "Check Conversation about mistakes in Highscore-Game")
    @PostMapping("/highscore/{conversationId}")
    public ResponseEntity<List<MistakeResponseDTO>> postNewLanguageCheckForConversationInHighscoreGame(@PathVariable Long conversationId) {
        return new ResponseEntity<>(evaluationService.receiveMistakesForHighscoreMessage(conversationId), HttpStatus.OK);
    }
}
