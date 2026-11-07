package nl.softworks.calendarAggregator.infrastructure.mcp;

import nl.softworks.calendarAggregator.domain.interfaces.S;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.time.LocalDate;
import java.util.function.Consumer;

/**
 * Sends a flyer image to a vision model and returns its raw answer, which contains a
 * transcription and the same events XML the text scrapers use.
 */
@Service
public class FlyerVisionReader {

    /** Bump when the prompt changes; it is part of the cache key. */
    public static final String PROMPT_VERSION = "flyer-v1";

    @Value("${calendaraggregator.aiAgent.visionModel:deepseek/deepseek-v4-flash-vision-exp}")
    private String visionModel;

    public String promptVersion() {
        return PROMPT_VERSION;
    }

    public String model() {
        return visionModel;
    }

    public String read(byte[] imageBytes, String mimeType, LocalDate today, String timezone, Consumer<String> log) {
        String systemPrompt = """
                You read event posters and flyers. Transcribe the visible text and extract every event.
                Today is %s (%s). Resolve dates without a year against today and never return events in the past.
                If a time is missing, assume the evening: 20:00 until 23:00.

                Return exactly this structure:
                <transcription>The visible text of the flyer</transcription>
                <events>
                    <event>
                        <startDate>2024-01-01</startDate>
                        <endDate>2024-01-02</endDate>
                        <startTime>10:00</startTime>
                        <endTime>12:00</endTime>
                        <description>Dancing is fun</description>
                    </event>
                </events>
                Dates must be in the format yyyy-MM-dd. Times must be in the format HH:mm.
                If the flyer contains no events, return an empty <events></events>.
                """.formatted(today, timezone);

        log.accept("Flyer vision: " + visionModel + ", " + imageBytes.length + " bytes, " + mimeType + "\n");
        String response = ChatClient
                .create(S.chatModel())
                .prompt()
                .options(OpenAiChatOptions.builder().model(visionModel))
                .system(systemPrompt)
                .user(user -> user
                        .text("Read this flyer and return the transcription and events.")
                        .media(MimeTypeUtils.parseMimeType(mimeType), new NamedByteArrayResource(imageBytes, mimeType)))
                .call()
                .content();
        log.accept("Flyer vision result: " + response + "\n");
        return response;
    }

    private static final class NamedByteArrayResource extends ByteArrayResource {
        private final String filename;

        private NamedByteArrayResource(byte[] byteArray, String mimeType) {
            super(byteArray);
            this.filename = "flyer" + extension(mimeType);
        }

        @Override
        public String getFilename() {
            return filename;
        }

        private static String extension(String mimeType) {
            if ("image/png".equals(mimeType)) return ".png";
            if ("image/jpeg".equals(mimeType)) return ".jpg";
            if ("image/gif".equals(mimeType)) return ".gif";
            if ("image/webp".equals(mimeType)) return ".webp";
            return "";
        }
    }
}