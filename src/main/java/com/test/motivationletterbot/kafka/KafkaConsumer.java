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
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import com.test.motivationletterbot.entity.UserSession;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.pdfbox.pdmodel.font.PDFont;

@Slf4j
@Service
@Getter
public class KafkaConsumer {
    private final MotivationLetterBot motivationLetterBot;
    private final KafkaTemplate<String, KafkaResponse> responseKafkaTemplate;
    private final String responseTopic;
    private final LlmClient llmClient;

    // cached font bytes to avoid repeated file reads; used to create PDType0Font per document
    private final AtomicReference<byte[]> cachedFontData = new AtomicReference<>();

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

        motivationLetterBot.sendPdf(response.getChatId(), response.getState(), pdfFile);
        // remove temp file after sending (best-effort)
        try {
            Files.deleteIfExists(pdfFile.toPath());
        } catch (IOException e) {
            log.warn("Failed to delete temp PDF {}: {}", pdfFile, e.getMessage());
        }
    }

    private File generatePdf(String generatedText) throws IOException {
        // Create a temp file per request to avoid conflicts
        Path tmp = Files.createTempFile("motivation-letter-", ".pdf");
        File outputFile = tmp.toFile();

        try (PDDocument document = new PDDocument()) {
            PDFont font = createDocumentFont(document);

            float margin = 50f;
            float yStart = PDRectangle.A4.getHeight() - margin;
            float leading = 16f;
            int fontSize = 12;

            // Prepare wrapped lines from the text
            List<String> lines = wrapTextToLines(generatedText, 80); // simple char-based wrap

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = null;
            try {
                contentStream = new PDPageContentStream(document, page);
                contentStream.beginText();
                contentStream.setFont(font, fontSize);
                contentStream.setLeading(leading);
                contentStream.newLineAtOffset(margin, yStart);

                float yPosition = yStart;

                for (String line : lines) {
                    if (yPosition - leading < margin) {
                        // finish current page
                        contentStream.endText();
                        contentStream.close();

                        // new page
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        contentStream.beginText();
                        contentStream.setFont(font, fontSize);
                        contentStream.setLeading(leading);
                        contentStream.newLineAtOffset(margin, yStart);
                        yPosition = yStart;
                    }

                    // If using a Type1 fallback font, sanitize unsupported glyphs. If using PDType0Font (TTF), write raw Unicode.
                    String safe;
                    if (font instanceof PDType0Font) {
                        safe = line;
                    } else {
                        safe = sanitizeForFont(line, font);
                    }
                    contentStream.showText(safe == null ? "" : safe);
                    contentStream.newLine();
                    yPosition -= leading;
                }

                contentStream.endText();
            } finally {
                if (contentStream != null) {
                    try {
                        contentStream.close();
                    } catch (IOException ignored) {
                        // ignore
                    }
                }
            }

            document.save(outputFile);
        }

        return outputFile;
    }

    private List<String> wrapTextToLines(String text, int maxLineLength) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isEmpty()) return result;

        String[] paragraphs = text.split("\\r?\\n");
        for (String paragraph : paragraphs) {
            if (paragraph == null || paragraph.isEmpty()) {
                result.add("");
                continue;
            }
            String[] words = paragraph.split("\\s+");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                if (line.length() + word.length() + 1 > maxLineLength) {
                    result.add(line.toString().trim());
                    line.setLength(0);
                }
                if (line.length() > 0) line.append(' ');
                line.append(word);
            }
            if (line.length() > 0) result.add(line.toString());
            // paragraph gap
            result.add("");
        }
        return result;
    }

    // Create PDType0Font per document using DejaVuSans.ttf from resources when available.
    // If not available, fall back to a standard Type1 font (Helvetica) which may not support Cyrillic.
    private PDFont createDocumentFont(PDDocument document) {
        InputStream is = getClass().getResourceAsStream("/fonts/DejaVuSans.ttf");
        if (is != null) {
            try (InputStream in = is) {
                return PDType0Font.load(document, in, true);
            } catch (IOException e) {
                log.warn("Failed to load DejaVuSans.ttf from resources: {}", e.getMessage());
            }
        }

        log.warn("DejaVuSans.ttf not found or failed to load; falling back to Helvetica (may not support Cyrillic)");
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    }

    // Replace characters that are not available in the chosen font. Special-case some common smart punctuation.
    private String sanitizeForFont(String input, PDFont font) {
        if (input == null || input.isEmpty()) return input;
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            int cp = input.codePointAt(i);
            String ch = new String(Character.toChars(cp));
            try {
                // try to encode this codepoint using the font
                font.encode(ch);
                sb.append(ch);
            } catch (Exception e) {
                // fallback mappings for known punctuation
                if (cp == 0x2011) { // non-breaking hyphen
                    sb.append('-');
                } else if (cp == 0x2013 || cp == 0x2014) { // en-dash/em-dash
                    sb.append('-');
                } else if (cp == 0x2018 || cp == 0x2019 || cp == 0x201C || cp == 0x201D) { // smart quotes
                    sb.append('"');
                } else {
                    // replace unsupported char with a simple placeholder
                    sb.append('?');
                }
            }
            i += Character.charCount(cp);
        }
        return sb.toString();
    }

}
