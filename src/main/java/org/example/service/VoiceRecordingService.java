package org.example.service;

import org.example.entity.VoiceRecording;
import org.example.repository.VoiceRecordingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    public void initSampleDataIfEmpty() {
        if (voiceRecordingRepository.count() == 0) {
            log.info("Initializing sample data...");
            
            VoiceRecording recording1 = new VoiceRecording(
                "Acme Corporation",
                "Meeting with Acme Corporation regarding Q4 budget allocation and project timeline for the new product launch.",
                "acme_meeting.wav",
                "audio/wav",
                2048576L
            );
            
            VoiceRecording recording2 = new VoiceRecording(
                "Acme Corporation",
                "Follow-up call with Acme Corporation about contract renewal terms and pricing adjustments for next year.",
                "acme_followup.wav",
                "audio/wav",
                1536000L
            );
            
            VoiceRecording recording3 = new VoiceRecording(
                "GlobalTech Industries",
                "Initial discussion with GlobalTech Industries about partnership opportunities and technology integration.",
                "globaltech_intro.wav",
                "audio/wav",
                3145728L
            );
            
            VoiceRecording recording4 = new VoiceRecording(
                "GlobalTech Industries",
                "Technical review session with GlobalTech Industries engineering team for API integration specifications.",
                "globaltech_tech_review.wav",
                "audio/wav",
                2560000L
            );
            
            VoiceRecording recording5 = new VoiceRecording(
                "Meridian Healthcare",
                "Consultation call with Meridian Healthcare regarding patient data management system requirements.",
                "meridian_consultation.wav",
                "audio/wav",
                1843200L
            );
            
            VoiceRecording recording6 = new VoiceRecording(
                "Meridian Healthcare",
                "Contract negotiation with Meridian Healthcare for software licensing and support agreement.",
                "meridian_negotiation.wav",
                "audio/wav",
                2252800L
            );
            
            VoiceRecording recording7 = new VoiceRecording(
                "Sunrise Retail Group",
                "Quarterly business review with Sunrise Retail Group covering sales performance and inventory optimization.",
                "sunrise_qbr.wav",
                "audio/wav",
                2867200L
            );

            voiceRecordingRepository.saveAll(List.of(recording1, recording2, recording3, recording4, recording5, recording6, recording7));
            log.info("Sample data initialized successfully");
        }
    }
}