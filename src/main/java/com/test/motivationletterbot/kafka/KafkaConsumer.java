package com.test.motivationletterbot.kafka;

import com.test.motivationletterbot.bot.MotivationLetterBot;
import com.test.motivationletterbot.llm.LlmClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import com.test.motivationletterbot.entity.UserSession;

import java.io.File;
import java.io.IOException;

@Slf4j
@Service
@Getter
public class KafkaConsumer {
    private final MotivationLetterBot motivationLetterBot;
    private final KafkaTemplate<String, KafkaResponse> responseKafkaTemplate;
    private final String responseTopic;
    private final LlmClient llmClient;

    public KafkaConsumer(
            @Lazy MotivationLetterBot motivationLetterBot,
            KafkaTemplate<String, KafkaResponse> responseKafkaTemplate,
            @Value("${motivation-bot.kafka.response-topic}") String responseTopic,
            @Qualifier("openAiClientService") LlmClient llmClient) {
        this.motivationLetterBot = motivationLetterBot;
        this.responseKafkaTemplate = responseKafkaTemplate;
        this.responseTopic = responseTopic;
        this.llmClient = llmClient;
    }

    @KafkaListener(topics = "${motivation-bot.kafka.request-topic}", groupId = "motivation-letter-bot")
    public void handleRequest(KafkaRequest request) throws Exception {
        String generatedReply = llmClient.sendPrompt(request.getText());
        KafkaResponse response = new KafkaResponse(request.getChatId(), generatedReply, request.getState());
        responseKafkaTemplate.send(responseTopic, response);
    }

    @KafkaListener(topics = "${motivation-bot.kafka.response-topic}", groupId = "motivation-letter-bot")
    public void handleResponse(KafkaResponse response) throws IOException {
        File pdfFile = generatePdf(response.getGeneratedText());
        // Fetch live in-memory session from the bot to avoid stale/serialized session state
        UserSession liveSession = motivationLetterBot.getUserSessions().get(response.getChatId());
        motivationLetterBot.sendPdf(response.getChatId(), response.getState(), pdfFile);
    }

    private File generatePdf(String generatedText) throws IOException {
        // Create new PDF document
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDType1Font font = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);

        // Define output file
        File outputFile = new File("Balkouski, Motivational Letter.pdf");

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.setLeading(16f); // line spacing
            contentStream.newLineAtOffset(50, 750); // start position (50 from left, 750 from bottom)

            int maxLineLength = 80; // adjust as needed

            // Split text into paragraphs by line breaks
            String[] paragraphs = generatedText.split("\\r?\\n");
            for (String paragraph : paragraphs) {
                String[] words = paragraph.split(" ");
                StringBuilder line = new StringBuilder();

                for (String word : words) {
                    if (line.length() + word.length() > maxLineLength) {
                        contentStream.showText(line.toString().trim());
                        contentStream.newLine();
                        line = new StringBuilder();
                    }
                    line.append(word).append(" ");
                }

                // Print last line of the paragraph
                if (!line.isEmpty()) {
                    contentStream.showText(line.toString().trim());
                    contentStream.newLine();
                }

                // Extra new line for paragraph spacing
                contentStream.newLine();
            }

            contentStream.endText();
        }

        // Save and close
        document.save(outputFile);
        document.close();

        return outputFile;
    }

}
