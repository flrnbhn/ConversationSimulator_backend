package org.flbohn.conversationsimulator_backend.llmservices;

import org.flbohn.conversationsimulator_backend.conversation.domain.Message;
import org.flbohn.conversationsimulator_backend.conversation.types.ConversationMember;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletion;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionMessage;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionMessage.Role;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OpenAiService {

    final private OpenAiApi openAiApi;

    public OpenAiService() {
        openAiApi = new OpenAiApi(System.getenv("SPRING_AI_OPENAI_API_KEY"));
    }

    public String sendMessage(List<Message> allMessages) {
        String message;
        List<ChatCompletionMessage> chatCompletionMessageList = createChatCompletionMessageList(allMessages);
        ResponseEntity<ChatCompletion> response = openAiApi.chatCompletionEntity(
                new ChatCompletionRequest(chatCompletionMessageList, "gpt-3.5-turbo", 0.8f, false));

        if (response.getBody() != null) {
            message = response.getBody().choices().getFirst().message().content();
        } else {
            message = "Error when retrieving data";
        }
        return message;
    }

    private List<ChatCompletionMessage> createChatCompletionMessageList(List<Message> allMessages) {
        List<ChatCompletionMessage> chatCompletionMessageList = new ArrayList<>();
        allMessages.forEach(message ->
                chatCompletionMessageList.add(new ChatCompletionMessage(message.getMessage(), decideRole(message.getConversationMember()))));

        return chatCompletionMessageList;
    }

    private Role decideRole(ConversationMember conversationMember) {
        return switch (conversationMember) {
            case USER -> Role.USER;
            case PARTNER -> Role.ASSISTANT;
            case SYSTEM -> Role.SYSTEM;
            case NONE -> null;
        };
    }
}
