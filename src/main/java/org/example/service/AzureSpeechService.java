package org.example.service;

import org.example.config.AzureSpeechConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class AzureSpeechService {

    private static final Logger log = LoggerFactory.getLogger(AzureSpeechService.class);

    private final RestClient restClient;
    private final String language;
    private final boolean configured;

    @Autowired
    public AzureSpeechService(RestClient restClient, AzureSpeechConfig config) {
        this.restClient = restClient;
        this.language = config.getLanguage();
        this.configured = config.isConfigured();
    }

    public String transcribeAudio(MultipartFile audioFile) throws IOException {
        if (!configured) {
            log.warn("Azure Speech not configured, returning mock transcription");
            return "This is a mock transcription for testing. Business partner Acme Corporation mentioned in recording.";
        }

        log.info("Starting transcription for file: {} (size: {} bytes, type: {})", 
                audioFile.getOriginalFilename(), audioFile.getSize(), audioFile.getContentType());

        byte[] audioBytes = audioFile.getBytes();
        
        String contentType = audioFile.getContentType();
        if (contentType == null) {
            contentType = "audio/wav";
        }

        String url = "/speech/recognition/conversation/cognitiveservices/v1?language=" + language;

        String response = restClient.post()
                .uri(url)
                .contentType(MediaType.parseMediaType(contentType))
                .body(audioBytes)
                .retrieve()
                .body(String.class);

        log.debug("Azure Speech API response: {}", response);
        return parseTranscription(response);
    }

    private String parseTranscription(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "";
        }

        try {
            int displayTextIndex = response.indexOf("\"DisplayText\"");
            if (displayTextIndex >= 0) {
                int start = response.indexOf(':', displayTextIndex) + 1;
                int end = response.indexOf('"', start + 1);
                if (start > 0 && end > start) {
                    String text = response.substring(start, end).trim();
                    return text.replace("\\\"", "\"").replace("\\\\", "\\");
                }
            }

            int statusIndex = response.indexOf("\"RecognitionStatus\"");
            if (statusIndex >= 0) {
                int start = response.indexOf(':', statusIndex) + 1;
                int end = response.indexOf('"', start + 1);
                if (start > 0 && end > start) {
                    String status = response.substring(start, end).trim();
                    if (!"Success".equalsIgnoreCase(status)) {
                        log.warn("Speech recognition status: {}", status);
                    }
                }
            }

            return "";
        } catch (Exception e) {
            log.error("Failed to parse transcription response", e);
            return "";
        }
    }

    public String extractBusinessPartner(String transcription) {
        if (transcription == null || transcription.trim().isEmpty()) {
            return "Unknown";
        }

        String lowerTranscription = transcription.toLowerCase();
        
        String[] businessKeywords = {
            "partner", "company", "corporation", "inc", "llc", "ltd", "limited",
            "business", "enterprise", "firm", "organization", "org"
        };

        for (String keyword : businessKeywords) {
            int keywordIndex = lowerTranscription.indexOf(keyword);
            if (keywordIndex >= 0) {
                int startIndex = Math.max(0, keywordIndex - 30);
                int endIndex = Math.min(lowerTranscription.length(), keywordIndex + keyword.length() + 50);
                String context = transcription.substring(startIndex, endIndex).trim();
                
                String[] words = context.split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    if (words[i].equalsIgnoreCase(keyword) && i > 0) {
                        StringBuilder partnerName = new StringBuilder();
                        for (int j = Math.max(0, i - 3); j <= i; j++) {
                            if (j > 0) partnerName.append(" ");
                            partnerName.append(words[j].replaceAll("[^a-zA-Z0-9]", ""));
                        }
                        return partnerName.toString().trim();
                    }
                }
            }
        }

        String[] words = transcription.split("\\s+");
        if (words.length >= 2) {
            StringBuilder potentialName = new StringBuilder();
            for (int i = 0; i < Math.min(3, words.length); i++) {
                if (i > 0) potentialName.append(" ");
                potentialName.append(words[i].replaceAll("[^a-zA-Z0-9]", ""));
            }
            return potentialName.toString().trim();
        }

        return transcription.length() > 50 ? transcription.substring(0, 50) : transcription;
    }
}