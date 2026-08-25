package org.example.service;

import org.example.entity.VoiceRecording;
import org.example.repository.VoiceRecordingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class VoiceRecordingService {

    private static final Logger log = LoggerFactory.getLogger(VoiceRecordingService.class);

    private final AzureSpeechService azureSpeechService;
    private final VoiceRecordingRepository voiceRecordingRepository;

    public VoiceRecordingService(AzureSpeechService azureSpeechService, VoiceRecordingRepository voiceRecordingRepository) {
        this.azureSpeechService = azureSpeechService;
        this.voiceRecordingRepository = voiceRecordingRepository;
    }

    public VoiceRecording processVoiceRecording(MultipartFile audioFile) {
        log.info("Processing voice recording: {}", audioFile.getOriginalFilename());

        String transcription;
        try {
            transcription = azureSpeechService.transcribeAudio(audioFile);
        } catch (Exception e) {
            log.error("Failed to transcribe audio", e);
            throw new RuntimeException("Failed to transcribe audio: " + e.getMessage(), e);
        }

        String businessPartner = azureSpeechService.extractBusinessPartner(transcription);
        log.info("Extracted business partner: {}", businessPartner);

        VoiceRecording recording = new VoiceRecording(
            businessPartner,
            transcription,
            audioFile.getOriginalFilename(),
            audioFile.getContentType(),
            audioFile.getSize()
        );

        VoiceRecording saved = voiceRecordingRepository.save(recording);
        log.info("Saved voice recording with ID: {}", saved.getId());
        
        return saved;
    }

    public Iterable<VoiceRecording> getAllRecordings() {
        return voiceRecordingRepository.findAll();
    }

    public VoiceRecording getRecordingById(Long id) {
        return voiceRecordingRepository.findById(id).orElse(null);
    }

    public Iterable<VoiceRecording> getRecordingsByBusinessPartner(String businessPartner) {
        return voiceRecordingRepository.findByBusinessPartnerContainingIgnoreCase(businessPartner);
    }
}