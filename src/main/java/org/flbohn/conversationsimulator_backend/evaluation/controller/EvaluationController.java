package org.flbohn.conversationsimulator_backend.evaluation.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.flbohn.conversationsimulator_backend.evaluation.dto.EvaluationResponseDTO;
import org.flbohn.conversationsimulator_backend.evaluation.dto.MistakeResponseDTO;
import org.flbohn.conversationsimulator_backend.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/evaluation")
public class EvaluationController {

    private final EvaluationService evaluationService;

    @Autowired
    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @Operation(summary = "Check Language")
    @PostMapping("/test")
    public ResponseEntity<String> postNewLanguageCheckTest(@RequestBody String checkingString) {
        return new ResponseEntity<>(evaluationService.checkLanguage(checkingString), HttpStatus.OK);
    }

    @Operation(summary = "Check Sentence about mistakes")
    @PostMapping("")
    public ResponseEntity<List<MistakeResponseDTO>> postNewLanguageCheck(@RequestBody String checkingString) {
        return new ResponseEntity<>(evaluationService.receiveMistakes(checkingString), HttpStatus.OK);
    }

    @Operation(summary = "Check Conversation about mistakes")
    @PostMapping("/{conversationId}")
    public ResponseEntity<EvaluationResponseDTO> postNewLanguageCheckForConversation(@PathVariable Long conversationId) {
        return new ResponseEntity<>(evaluationService.receiveMistakesByConversation(conversationId), HttpStatus.OK);
    }
}
