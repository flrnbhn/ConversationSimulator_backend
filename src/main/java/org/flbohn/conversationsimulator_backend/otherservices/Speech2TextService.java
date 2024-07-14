package org.flbohn.conversationsimulator_backend.otherservices;

import org.springframework.ai.openai.OpenAiAudioTranscriptionClient;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.openai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.transcribe.TranscribeClient;

import java.util.Base64;

/**
 * Service which is responsible for transcription. Communicates with OpenAi.
 */
@Service
public class Speech2TextService {


    private final OpenAiAudioTranscriptionClient openAiAudioTranscriptionClient;

    @Autowired
    public Speech2TextService(TranscribeClient transcribeClient, S3Client s3Client) {
        this.openAiAudioTranscriptionClient = new OpenAiAudioTranscriptionClient(new OpenAiAudioApi(System.getenv("SPRING_AI_OPENAI_API_KEY")));
    }


    /**
     * transcribes a base64 encoded audio string into a text message
     */
    public String transcription(String base64EncodedAudio) {
        byte[] audioBytes = Base64.getDecoder().decode(base64EncodedAudio);
        Resource audioResource = new ByteArrayResource(audioBytes);
        OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
                .withResponseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .withTemperature(0f)
                .build();
        AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(audioResource, transcriptionOptions);
        AudioTranscriptionResponse response = openAiAudioTranscriptionClient.call(transcriptionRequest);
        return response.getResult().getOutput();
    }
}