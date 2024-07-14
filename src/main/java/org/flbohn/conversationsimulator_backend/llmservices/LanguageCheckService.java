package org.flbohn.conversationsimulator_backend.llmservices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flbohn.conversationsimulator_backend.conversation.service.ConversationServiceImpl;
import org.flbohn.conversationsimulator_backend.evaluation.dto.MistakeResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class LanguageCheckService {

    private WebClient webClient;

    private final OpenAiApi openAiApi;
    private static final Logger log = LoggerFactory.getLogger(ConversationServiceImpl.class);


    @Autowired
    public LanguageCheckService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.languagetoolplus.com/v2").build();
        this.openAiApi = new OpenAiApi(System.getenv("SPRING_AI_OPENAI_API_KEY"));

    }

    public Mono<String> checkLanguage(String text) {
        return webClient.post()
                .uri("/check")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("accept", "application/json")
                .bodyValue("text=" + text + "&language=auto" + "&enabledOnly=false")
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<List<MistakeResponseDTO>> checkConversation_text(String text) {
        return webClient.post()
                .uri("/check")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("accept", "application/json")
                .bodyValue("text=" + text + "&language=auto"
                        + "&disabledCategories=PUNCTUATION, CASING")
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(this::parseResult);
    }

    public Mono<List<MistakeResponseDTO>> checkConversation_audio(String text) {
        return webClient.post()
                .uri("/check")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("accept", "application/json")
                .bodyValue("text=" + text + "&language=auto"
                        + "&disabledCategories=TYPOS,PUNCTUATION,COMPOUNDING,CASING")
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(this::parseResult);
    }


    private Mono<List<MistakeResponseDTO>> parseResult(String jsonResponse) {
        List<MistakeResponseDTO> mistakeResponseDTOS = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode matchesNode = rootNode.get("matches");

            if (matchesNode != null && matchesNode.isArray()) {
                for (JsonNode matchNode : matchesNode) {
                    String message = matchNode.path("message").asText();
                    String shortMessage = matchNode.path("shortMessage").asText();
                    Integer offset = matchNode.path("offset").asInt();
                    Integer length = matchNode.path("length").asInt();
                    String sentence = matchNode.path("sentence").asText();
                    List<String> replacements = new ArrayList<>();
                    for (JsonNode replacementNode : matchNode.path("replacements")) {
                        replacements.add(replacementNode.path("value").asText());
                    }
                    MistakeResponseDTO mistakeResponseDTO = new MistakeResponseDTO(message, shortMessage, replacements, offset, length, sentence);
                    mistakeResponseDTOS.add(mistakeResponseDTO);
                }
            }

        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
        return Mono.just(mistakeResponseDTOS);
    }


    private List<MistakeResponseDTO> parseResultForGPT(String jsonResponse, String text, boolean isVoiceMessage) {
        List<MistakeResponseDTO> mistakeResponseDTOS = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(jsonResponse);
            JsonNode matchesNode = rootNode.get("matches");

            if (matchesNode != null && matchesNode.isArray()) {
                for (JsonNode matchNode : matchesNode) {
                    String message = matchNode.path("message").asText();
                    String shortMessage = matchNode.path("shortMessage").asText();
                    Integer offset = matchNode.path("offset").asInt();
                    Integer length = matchNode.path("length").asInt();
                    String sentence = matchNode.path("sentence").asText();
                    List<String> replacements = new ArrayList<>();
                    for (JsonNode replacementNode : matchNode.path("replacements")) {
                        replacements.add(replacementNode.path("value").asText());
                    }
                    MistakeResponseDTO mistakeResponseDTO = new MistakeResponseDTO(message, shortMessage, replacements, offset, length, sentence);
                    mistakeResponseDTOS.add(mistakeResponseDTO);
                }
            }
        } catch (JsonProcessingException e) {
            if (isVoiceMessage) {
                mistakeResponseDTOS = checkConversation_audio(text).block();
            } else {
                mistakeResponseDTOS = checkConversation_text(text).block();
            }
            log.error(String.valueOf(e));
        }


        return mistakeResponseDTOS;
    }

    public List<MistakeResponseDTO> doGrammarCheckWithOpenAi(String text, boolean isVoiceMessage) {
        List<OpenAiApi.ChatCompletionMessage> chatCompletionMessageList = new ArrayList<>();
        OpenAiApi.ChatCompletionMessage chatCompletionMessage = new OpenAiApi.ChatCompletionMessage(createExplanation(text, isVoiceMessage), OpenAiApi.ChatCompletionMessage.Role.SYSTEM);
        chatCompletionMessageList.add(chatCompletionMessage);
        return parseResultForGPT(callOpenAi(chatCompletionMessageList, "gpt-4-turbo"), text, isVoiceMessage);

    }


    private String callOpenAi(List<OpenAiApi.ChatCompletionMessage> chatCompletionMessageList, String gpt_model) {
        String message;
        ResponseEntity<OpenAiApi.ChatCompletion> response = openAiApi.chatCompletionEntity(
                new OpenAiApi.ChatCompletionRequest(chatCompletionMessageList, gpt_model, 0.9f, false));

        if (response.getBody() != null) {
            message = response.getBody().choices().getFirst().message().content();
        } else {
            message = "Error when retrieving data";
        }
        return message;
    }

    private String createExplanation(String text, boolean isVoiceMessage) {
        String explanation;
        if (isVoiceMessage) {
            explanation = "You are a Grammar check tool. Check the following sentence only for grammar mistakes: " + text + "\n That means you ignore spelling mistakes, punctuation errors and capitalization. Correct only if something is grammatically incorrect.";

        } else {
            explanation = "You are a Grammar and Spell,  check tool. Check the following sentence for grammar and spelling (punctuation, capitalization, wrong spelled words etc) mistakes: " + text + "\n";

        }
        String returnExplanation = "Returns only a string in this structure as the response: ";
        String jsonStructure = "{\n" +
                "    \"matches\": [\n" +
                "        {\n" +
                "            \"message\": \"This is an error message.\",\n" +
                "            \"shortMessage\": \"Error\",\n" +
                "            \"offset\": 5,\n" +
                "            \"length\": 4,\n" +
                "            \"sentence\": \"This is an error sentence.\",\n" +
                "            \"replacements\": [\n" +
                "                {\n" +
                "                    \"value\": \"correction1\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"value\": \"correction2\"\n" +
                "                }\n" +
                "            ]\n" +
                "        },\n" +
                "        {\n" +
                "            \"message\": \"Another mistake.\",\n" +
                "            \"shortMessage\": \"Mistake\",\n" +
                "            \"offset\": 15,\n" +
                "            \"length\": 7,\n" +
                "            \"sentence\": \"Here is another mistake.\",\n" +
                "            \"replacements\": [\n" +
                "                {\n" +
                "                    \"value\": \"fix1\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"value\": \"fix2\"\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}\n";
        String language = "Reproduce the description of \"message\" and \"short message\" in German.\n";
        String noError = "if no error can be found in the sentence, simply return an empty json structure -> {}";

        return explanation + returnExplanation + jsonStructure + language + noError;
    }
}


