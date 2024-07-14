package org.flbohn.conversationsimulator_backend.conversation.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.flbohn.conversationsimulator_backend.conversation.dto.conversation.ConversationRequestDTO;
import org.flbohn.conversationsimulator_backend.conversation.dto.conversation.ConversationStatusRequestDTO;
import org.flbohn.conversationsimulator_backend.conversation.dto.conversation.HighScoreConversationResponseDTO;
import org.flbohn.conversationsimulator_backend.conversation.dto.message.MessageRequestDTO;
import org.flbohn.conversationsimulator_backend.conversation.dto.message.MessageResponseDTO;
import org.flbohn.conversationsimulator_backend.conversation.dto.transcription.TranscriptionRequestDTO;
import org.flbohn.conversationsimulator_backend.conversation.service.ConversationService;
import org.flbohn.conversationsimulator_backend.exercise.dto.task.TaskResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * RestController rest controller for the execution of a conversation.
 */

@RestController
@RequestMapping("/conversation")
public class ConversationController {

    private static final Logger log = LoggerFactory.getLogger(ConversationController.class);
    private final ConversationService conversationService;

    @Autowired
    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @Operation(summary = "Client can create a new conversation by posting the start date of the conversation (wird noch erweitert)")
    @PostMapping("")
    public ResponseEntity<Long> postNewConversation(@RequestBody ConversationRequestDTO conversationRequestDTO) {
        return new ResponseEntity<>(conversationService.createConversation(conversationRequestDTO.conversationStartDate(), conversationRequestDTO.exerciseId(), conversationRequestDTO.learnerId()).getId(), HttpStatus.OK);
    }

    @Operation(summary = "Init Conversation to initiate the conversation, to tell llm sends the first message")
    @GetMapping("/init/{conversationId}")
    public ResponseEntity<MessageResponseDTO> initConversation(@PathVariable Long conversationId) {
        try {
            return new ResponseEntity<>(MessageResponseDTO.from(conversationService.initConversation(conversationId), conversationService.synthesizeMessageFromConversation(conversationId)), HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Client can send new message and get llm-generated response message")
    @PostMapping("/newMessage")
    public ResponseEntity<MessageResponseDTO> postNewMessage(@RequestBody MessageRequestDTO message) {
        try {
            return new ResponseEntity<>(MessageResponseDTO.from(conversationService.createMessage(message.message(), message.conversationMember(), message.conversationID(), message.isAudioMessage()),
                    conversationService.synthesizeMessageFromConversation(message.conversationID())),
                    HttpStatus.OK);
        } catch (NoSuchElementException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Get all finished Tasks")
    @GetMapping("/finishedTasks/{conversationId}")
    public ResponseEntity<List<TaskResponseDTO>> getFinishedTasks(@PathVariable Long conversationId) {
        return new ResponseEntity<>(conversationService.getFinishedTasks(conversationId).stream()
                .map(TaskResponseDTO::from)
                .toList(), HttpStatus.OK);
    }

    @Operation(summary = "Change conversation status")
    @PostMapping("/conversationStatus/{conversationId}")
    public ResponseEntity<Void> changeConversationStatus(@PathVariable Long conversationId, @RequestBody ConversationStatusRequestDTO conversationStatusDTO) {
        if (conversationService.changeConversationStatus(conversationId, conversationStatusDTO.conversationStatus())) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Create new Highscore conversation")
    @PostMapping("/highscore")
    public ResponseEntity<HighScoreConversationResponseDTO> postHighscoreConversation(@RequestBody ConversationRequestDTO conversationRequestDTO) {
        return new ResponseEntity<>(HighScoreConversationResponseDTO.from(conversationService.createHighScoreConversation(conversationRequestDTO.conversationStartDate(), conversationRequestDTO.learnerId())), HttpStatus.OK);
    }

    @Operation(summary = "Delete Conversation")
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long conversationId) {
        conversationService.deleteConversation(conversationId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/synthesize/{conversationId}")
    public ResponseEntity<String> synthesize(@PathVariable Long conversationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=output.mp3");
        headers.add(HttpHeaders.CONTENT_TYPE, "audio/mpeg");
        return new ResponseEntity<>(conversationService.synthesizeMessageFromConversation(conversationId), headers, HttpStatus.OK);
    }

    @Operation(summary = "Transcribe Base64-Message")
    @PostMapping("/transcribe")
    public ResponseEntity<String> transcribe(@RequestBody TranscriptionRequestDTO message) {
        return new ResponseEntity<>(conversationService.transcribeMessage(message.base64String()), HttpStatus.OK);
    }

    @Operation(summary = "Transcribe Base64-Message")
    @PostMapping("/translate/{conversationId}")
    public ResponseEntity<String> translate(@RequestBody String message, @PathVariable Long conversationId) {
        return new ResponseEntity<>(conversationService.translateMessage(message, conversationId), HttpStatus.OK);
    }
}
