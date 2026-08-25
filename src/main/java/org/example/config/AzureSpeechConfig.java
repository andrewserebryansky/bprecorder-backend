package org.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AzureSpeechConfig {

    @Value("${azure.speech.subscription-key:}")
    private String subscriptionKey;

    @Value("${azure.speech.region:}")
    private String region;

    @Value("${azure.speech.language:en-US}")
    private String language;

    @Bean
    public RestClient speechRestClient() {
        if (subscriptionKey.isEmpty() || region.isEmpty()) {
            return RestClient.builder().build();
        }
        String endpoint = String.format("https://%s.stt.speech.microsoft.com", region);
        return RestClient.builder()
                .baseUrl(endpoint)
                .defaultHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
                .defaultHeader("Content-Type", "audio/wav")
                .build();
    }

    public String getLanguage() {
        return language;
    }

    public String getRegion() {
        return region;
    }

    public boolean isConfigured() {
        return !subscriptionKey.isEmpty() && !region.isEmpty();
    }
}