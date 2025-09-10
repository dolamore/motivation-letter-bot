package com.test.motivationletterbot.kafka;

import com.test.motivationletterbot.bot.MotivationLetterBot;
import com.test.motivationletterbot.llm.LlmClient;
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

import java.io.File;
import java.io.IOException;

@Slf4j
@Service
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
        KafkaResponse response = new KafkaResponse(request.getChatId(), generatedReply, request.getSession(), request.getState());
        responseKafkaTemplate.send(responseTopic, response);
    }

    @KafkaListener(topics = "${motivation-bot.kafka.response-topic}", groupId = "motivation-letter-bot")
    public void handleResponse(KafkaResponse response) throws IOException {
        File pdfFile = generatePdf(response.getChatId(), response.getGeneratedText());
        motivationLetterBot.sendPdf(response.getChatId(), response.getSession(), response.getState(), pdfFile);
    }

    private File generatePdf(long chatId, String generatedText) throws IOException {
        // Create new PDF document
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        // Define output file
        File outputFile = new File("motivation_letter_" + chatId + ".pdf");

        // Write content
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

            // Title
            contentStream.beginText();
            contentStream.setFont(font, 18);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Motivation Letter");
            contentStream.endText();

            // Body text
            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.setLeading(16f); // line spacing
            contentStream.newLineAtOffset(50, 700);

            // Split text into lines so it fits on the page
            int maxLineLength = 80;
            String[] words = generatedText.split(" ");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                if (line.length() + word.length() > maxLineLength) {
                    contentStream.showText(line.toString());
                    contentStream.newLine();
                    line = new StringBuilder();
                }
                line.append(word).append(" ");
            }
            if (!line.isEmpty()) {
                contentStream.showText(line.toString());
            }

            contentStream.endText();
        }

        // Save and close
        document.save(outputFile);
        document.close();

        return outputFile;
    }
}
