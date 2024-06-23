package org.flbohn.conversationsimulator_backend.llmservices;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.openai.OpenAiAudioTranscriptionClient;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.openai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.UUID;

@Service
public class Speech2TextService {

    private final TranscribeClient transcribeClient;
    private final S3Client s3Client;
    private final String bucketName = "conversation-simulator-bucket";

    private final OpenAiApi openAiApi;
    private final OpenAiAudioTranscriptionClient openAiAudioTranscriptionClient;


    @Autowired
    public Speech2TextService(TranscribeClient transcribeClient, S3Client s3Client) {
        this.transcribeClient = transcribeClient;
        this.s3Client = s3Client;
        this.openAiAudioTranscriptionClient = new OpenAiAudioTranscriptionClient(new OpenAiAudioApi(System.getenv("SPRING_AI_OPENAI_API_KEY")));

        openAiApi = new OpenAiApi(System.getenv("SPRING_AI_OPENAI_API_KEY"));
    }

    public String transcribeAudioOpenAi(String base64EncodedAudio) {
        return null;
    }

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


    public String transcribeAudio(String base64EncodedAudio) {
        byte[] audioBytes = Base64.getDecoder().decode(base64EncodedAudio);

        String audioFileName = UUID.randomUUID().toString() + ".mp3";

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(audioFileName)
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(audioBytes));

        String mediaFileUri = String.format("s3://%s/%s", bucketName, audioFileName);

        Media media = Media.builder()
                .mediaFileUri(mediaFileUri)
                .build();

        StartTranscriptionJobRequest request = StartTranscriptionJobRequest.builder()
                .languageCode(LanguageCode.DE_DE) // Sprachcode anpassen
                .mediaFormat(MediaFormat.WAV) // Audio-Format anpassen, z.B. WAV, MP3, etc.
                .media(media)
                .transcriptionJobName("transcription-" + UUID.randomUUID()) // Job-Namen anpassen
                .outputBucketName(bucketName) // Ausgabe-Bucket anpassen
                .outputKey("transcriptions/" + audioFileName + ".json") // Ausgabe-Key anpassen
                .build();

        StartTranscriptionJobResponse response = transcribeClient.startTranscriptionJob(request);

        GetTranscriptionJobResponse transcriptionJobResponse;
        GetTranscriptionJobRequest getTranscriptionJobRequest = GetTranscriptionJobRequest.builder()
                .transcriptionJobName(response.transcriptionJob().transcriptionJobName())
                .build();
        do {
            transcriptionJobResponse = transcribeClient.getTranscriptionJob(getTranscriptionJobRequest);
        } while (transcriptionJobResponse.transcriptionJob().transcriptionJobStatus() == TranscriptionJobStatus.IN_PROGRESS);

        if (transcriptionJobResponse.transcriptionJob().transcriptionJobStatus() == TranscriptionJobStatus.COMPLETED) {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket("conversation-simulator-bucket")
                    .key("transcriptions/" + audioFileName + ".json")
                    .build();

            try (ResponseInputStream<GetObjectResponse> objectResponseInputStream = s3Client.getObject(getObjectRequest);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(objectResponseInputStream))) {

                StringBuilder transcriptionResult = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    transcriptionResult.append(line);
                }
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(transcriptionResult.toString());
                JsonNode transcriptsNode = jsonNode.get("results").get("transcripts");

                return transcriptsNode.get(0).get("transcript").asText();
            } catch (IOException e) {
                throw new RuntimeException("Error reading transcription result from S3", e);
            }
        } else {
            // Behandlung bei fehlgeschlagener Transkription
            throw new RuntimeException("Transcription failed: " + transcriptionJobResponse.transcriptionJob().failureReason());
        }
    }
}