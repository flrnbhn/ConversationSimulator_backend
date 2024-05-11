package org.flbohn.conversationsimulator_backend.conversation.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.flbohn.conversationsimulator_backend.conversation.domain.Message;
import org.flbohn.conversationsimulator_backend.conversation.dto.conversation.ConversationRequestDTO;
import org.flbohn.conversationsimulator_backend.conversation.dto.message.MessageRequestDTO;
import org.flbohn.conversationsimulator_backend.conversation.dto.message.MessageResponseDTO;
import org.flbohn.conversationsimulator_backend.conversation.service.ConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

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
        return new ResponseEntity<>(conversationService.createConversation(conversationRequestDTO.conversationStartDate(), conversationRequestDTO.exerciseId()).getId(), HttpStatus.OK);
    }

    @Operation(summary = "Init Conversation to initiate the conversation, to tell llm sends the first message")
    @GetMapping("/init/{conversationId}")
    public ResponseEntity<Message> initConversation(@PathVariable Long conversationId) {
        try {
            return new ResponseEntity<>(conversationService.initConversation(conversationId), HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Client can send new message and get llm-generated response message")
    @PostMapping("/newMessage")
    public ResponseEntity<MessageResponseDTO> postNewMessage(@RequestBody MessageRequestDTO message) {
        try {
            return new ResponseEntity<>(MessageResponseDTO.from(conversationService.createMessage(message.message(), message.conversationMember(), message.conversationID())), HttpStatus.OK);
        } catch (NoSuchElementException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
