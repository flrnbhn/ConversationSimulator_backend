package org.flbohn.conversationsimulator_backend.llmservices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flbohn.conversationsimulator_backend.evaluation.dto.MistakeResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class LanguageCheckService {

    private WebClient webClient;

    @Autowired
    public LanguageCheckService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.languagetoolplus.com/v2").build();
    }

    public Mono<String> checkLanguage(String language) {
        return webClient.post()
                .uri("/check")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("accept", "application/json")
                .bodyValue("text=" + language + "&language=auto" + "&enabledOnly=false")
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<List<MistakeResponseDTO>> checkConversation(String language) {
        return webClient.post()
                .uri("/check")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("accept", "application/json")
                .bodyValue("text=" + language + "&language=auto" + "&enabledOnly=false")
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
        return mistakeResponseDTOS.isEmpty() ? Mono.empty() : Mono.just(mistakeResponseDTOS);
    }
}
