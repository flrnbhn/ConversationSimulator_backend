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

    public String evaluateConversation(List<Message> allMessages, Exercise exercise) {
        List<ChatCompletionMessage> chatCompletionMessageList = new ArrayList<>();
        chatCompletionMessageList.add(new ChatCompletionMessage(createSystemExplanationStringForConversationEvaluation(exercise), Role.SYSTEM));

        String conversationString = allMessages.stream()
                .map(message -> message.getConversationMember() + ": " + message.getMessage())
                .collect(Collectors.joining("\n"));

        chatCompletionMessageList.add(new ChatCompletionMessage(conversationString, Role.USER));

        return callOpenAi(chatCompletionMessageList, "gpt-4-turbo");
    }

    private String createSystemExplanationStringForConversationEvaluation(Exercise exercise) {
        String introduction = " You are a conversation analysis tool that evaluates simulated conversations.  Analyze the following conversation regarding: " + exercise.getSzenario() + ". ";
        String roles = "The conversation involves a " + exercise.getRoleSystem() + " and a " + exercise.getRoleUser() + ". ";
        String classification = "The conversation trained " + exercise.getRoleUser() + "conversational skills ";
        String evaluation = "Your task is to find out what " + exercise.getRoleSystem() + "could have done better in the conversation";
        String criteria = "The following evaluation criteria apply: Vocabulary in conversation, relevance of content, cultural understanding";
        String task = "Give a brief description of the criteria, what was not good and what could have been done better";

        return introduction + roles + classification + evaluation + criteria + task;
    }


    public String initHighscoreConversation(Conversation conversation) {
        return sendHighscoreMessage(new ArrayList<>(), conversation);
    }

    public String sendHighscoreMessage(List<Message> allMessages, Conversation conversation) {
        List<ChatCompletionMessage> chatCompletionMessageList = createHighscoreChatCompletionMessageList(allMessages, conversation);
        return callOpenAi(chatCompletionMessageList, "gpt-4-turbo");
    }

    private List<ChatCompletionMessage> createHighscoreChatCompletionMessageList(List<Message> allMessages, Conversation conversation) {
        List<ChatCompletionMessage> chatCompletionMessageList = new ArrayList<>();
        chatCompletionMessageList.add(createHighscoreSystemExplanationPromptMessage(conversation));
        allMessages.forEach(message ->
                chatCompletionMessageList.add(new ChatCompletionMessage(message.getMessage(), decideRole(message.getConversationMember()))));

        return chatCompletionMessageList;
    }

    private ChatCompletionMessage createHighscoreSystemExplanationPromptMessage(Conversation conversation) {
        String message = createHighscoreSystemExplanationString(conversation);
        return new ChatCompletionMessage(message, Role.SYSTEM);
    }

    private String createHighscoreSystemExplanationString(Conversation conversation) {
        String introduction = "You are a conversation simulation tool. ";
        String task = "You will now talk to me and we have a conversation. You will be the leading part, guiding the conversation, so to speak, and setting the topics so that a nice conversation comes out of it. The conversation is endless. This means that when a topic is finished, suggest a new one. ";
        String length = " Keep it short, the length of your message should not exceed 100 words";
        String szenario = "The scenario of the conversation is as follows: " + conversation.getSzenario();
        String roles = " Your role is " + conversation.getRoleSystem() + "and my Role is" + conversation.getRoleSystem();
        String language = "The language of this conversation is " + conversation.getLearner().getLearningLanguage() + ". That means you and I talk in " + conversation.getLearner().getLearningLanguage();
        return introduction + task + szenario + roles + language + length;
    }

    public String createSzenario() {
        List<ChatCompletionMessage> chatCompletionMessageList = new ArrayList<>();
        String message = "Think of a conversation scenario in which long conversations can be held. Describe the scenario very briefly and say who the two conversation partners are.";
        ChatCompletionMessage chatCompletionMessage = new ChatCompletionMessage(message, Role.ASSISTANT);
        chatCompletionMessageList.add(chatCompletionMessage);
        return callOpenAi(chatCompletionMessageList, "gpt-4-turbo");
    }

    public String[] decideRole(String conversationSzenario) {
        List<ChatCompletionMessage> chatCompletionMessageList = new ArrayList<>();
        String szenario = "I have the following conversation scenario in which 2 conversation partners are also explained. Scenario: " + conversationSzenario;
        String decide = " Decide randomly which of the two conversation partners is the user and which is the system. Only return exactly one answer in this format: [user: conversation-partner1, system:  conversation-partner2]";
        ChatCompletionMessage chatCompletionMessage = new ChatCompletionMessage(szenario + decide, Role.ASSISTANT);
        chatCompletionMessageList.add(chatCompletionMessage);
        String answer = callOpenAi(chatCompletionMessageList, "gpt-4-turbo");
        return extractOpenAiString(answer);
    }

    public String[] extractOpenAiString(String answer) {
        String cleanedInput = answer.substring(1, answer.length() - 1);
        String[] parts = cleanedInput.split(", ");
        String[] names = new String[parts.length];
        for (int i = 0; i < parts.length; i++) {
            names[i] = parts[i].split(": ")[1];
        }
        return names;
    }
}
