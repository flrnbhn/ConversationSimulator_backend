package org.flbohn.conversationsimulator_backend.llmservices;

import org.flbohn.conversationsimulator_backend.conversation.domain.Conversation;
import org.flbohn.conversationsimulator_backend.conversation.domain.Message;
import org.flbohn.conversationsimulator_backend.conversation.types.ConversationMember;
import org.flbohn.conversationsimulator_backend.exercise.domain.Exercise;
import org.flbohn.conversationsimulator_backend.exercise.domain.Task;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletion;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionMessage;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionMessage.Role;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OpenAiService {

    final private OpenAiApi openAiApi;

    public OpenAiService() {
        openAiApi = new OpenAiApi(System.getenv("SPRING_AI_OPENAI_API_KEY"));
    }

    public String initConversation(Exercise exercise, Conversation conversation) {
        return sendMessage(new ArrayList<>(), exercise, conversation);
    }

    public String sendMessage(List<Message> allMessages, Exercise exercise, Conversation conversation) {
        List<ChatCompletionMessage> chatCompletionMessageList = createChatCompletionMessageList(allMessages, exercise, conversation);
        return callOpenAi(chatCompletionMessageList, "gpt-4-turbo");
    }

    private String callOpenAi(List<ChatCompletionMessage> chatCompletionMessageList, String gpt_model) {
        String message;
        ResponseEntity<ChatCompletion> response = openAiApi.chatCompletionEntity(
                new ChatCompletionRequest(chatCompletionMessageList, gpt_model, 0.9f, false));

        if (response.getBody() != null) {
            message = response.getBody().choices().getFirst().message().content();
        } else {
            message = "Error when retrieving data";
        }
        return message;
    }


    private List<ChatCompletionMessage> createChatCompletionMessageList(List<Message> allMessages, Exercise exercise, Conversation conversation) {
        List<ChatCompletionMessage> chatCompletionMessageList = new ArrayList<>();
        chatCompletionMessageList.add(createSystemExplanationPromptMessage(exercise, conversation));
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

    private ChatCompletionMessage createSystemExplanationPromptMessage(Exercise exercise, Conversation conversation) {
        String message = createSystemExplanationString(exercise, conversation);
        return new ChatCompletionMessage(message, Role.SYSTEM);
    }

    private String createSystemExplanationString(Exercise exercise, Conversation conversation) {
        String introduction = "You are a conversation simulation tool. We are now trying to simulate specific or situational conversations in order to learn foreign languages. The language for this conversation is " + conversation.getLearner().getLearningLanguage() + "! You are the simulated conversation partner and I am the learner. In order to give the conversation context and so that the conversation can be conducted by you, there are various tasks that I have to complete. ";
        String szenario = "The scenario in the following conversation situation is as follows: " + exercise.getSzenario() + " ";
        //ggf noch nummerieren
        String tasks = "The tasks that I have to complete one after the other, are as follows: " + exercise.getTasks().stream().map(Task::getDescription).collect(Collectors.joining(", ")) + " ";
        String explanationRole = "you have to take on the opposite role or guide the other person and me through the conversation, so to speak";
        String roles = "Your role is " + exercise.getRoleSystem() + "and my Role is" + exercise.getRoleSystem();
        String language = "The language of this conversation is " + conversation.getLearner().getLearningLanguage() + ". That means you and I talk in " + conversation.getLearner().getLearningLanguage();
        String conclusion = "Now start to take on your role and open the conversation ";
        return introduction + szenario + tasks + explanationRole + roles + language + conclusion;
    }

    public String evaluateTasksInConversation(List<Message> allMessages, Exercise exercise) {
        List<ChatCompletionMessage> chatCompletionMessageList = new ArrayList<>();
        chatCompletionMessageList.add(new ChatCompletionMessage(createSystemExplanationStringForTaskEvaluation(exercise), Role.SYSTEM));
      /*  allMessages.forEach(message ->
                chatCompletionMessageList.add(new ChatCompletionMessage(message.getMessage(), decideRole(message.getConversationMember()))));*/

        String conversationString = allMessages.stream()
                .map(message -> message.getConversationMember() + ": " + message.getMessage())
                .collect(Collectors.joining("\n"));

        chatCompletionMessageList.add(new ChatCompletionMessage(conversationString, Role.USER));

        //chatCompletionMessageList.add(new ChatCompletionMessage("Please give me back a comma-separated list of all points that have already been completed. Don't forget the points you have already mentioned", Role.USER));
        return callOpenAi(chatCompletionMessageList, "gpt-4-turbo");
    }

    private String createSystemExplanationStringForTaskEvaluation(Exercise exercise) {
        String introduction = "You are a conversation analysis tool. Analyze the following conversation regarding: " + exercise.getSzenario() + ". ";

        String roles = "The conversation involves a " + exercise.getRoleSystem() + " and a " + exercise.getRoleUser() + ". ";

        String tasks = "Your task is to identify which of the following points have been already discussed and completed: ";
        String taskList = exercise.getTasks().stream().map(Task::getDescription).collect(Collectors.joining(", "));
        String taskDescription = tasks + taskList + ". ";

        String taskClarification = "Please identify the points from the list I've given you that have already been discussed. Return only the completed points, separated by commas.";

        return introduction + roles + taskDescription + taskClarification;
    }
}
