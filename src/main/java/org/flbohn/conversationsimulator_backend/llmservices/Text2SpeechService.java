package org.flbohn.conversationsimulator_backend.llmservices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


@Service
public class Text2SpeechService {

    private final PollyClient pollyClient;

    @Autowired
    public Text2SpeechService(PollyClient pollyClient) {
        this.pollyClient = pollyClient;
    }

    public byte[] synthesizeSpeech(String text, String language, String gender) {
        DescribeVoicesRequest describeVoiceRequest = DescribeVoicesRequest.builder()
                .engine(Engine.NEURAL)
                .languageCode(decideLanguageCode(language))
                .build();

        DescribeVoicesResponse describeVoicesResult = pollyClient.describeVoices(describeVoiceRequest);
        Voice voice = describeVoicesResult.voices().stream()
                //.filter(v -> v.name().equals("Vicki"))
                .filter(v -> v.supportedEngines().contains(Engine.NEURAL))
                .filter(v -> v.genderAsString().toLowerCase().equals(gender))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Voice not found"));

        try {
            InputStream stream = synthesize(pollyClient, text, voice, OutputFormat.MP3);
            return convertInputStreamToByteArray(stream);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream synthesize(PollyClient polly, String text, Voice voice, OutputFormat format) throws IOException {
        SynthesizeSpeechRequest synthReq = SynthesizeSpeechRequest.builder()
                .text(text)
                .engine(Engine.NEURAL)
                .voiceId(voice.id())
                .outputFormat(format)
                .build();

        ResponseInputStream<SynthesizeSpeechResponse> synthRes = polly.synthesizeSpeech(synthReq);
        return synthRes;
    }

    private byte[] convertInputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private LanguageCode decideLanguageCode(String language) {
        return switch (language) {
            case "Englisch" -> LanguageCode.EN_US;
            case "Französisch" -> LanguageCode.FR_FR;
            case "Spanisch" -> LanguageCode.ES_ES;
            case "Deutsch" -> LanguageCode.DE_DE;
            default -> LanguageCode.valueOf(language);
        };
    }
}
