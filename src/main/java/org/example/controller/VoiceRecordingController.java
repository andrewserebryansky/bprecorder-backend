package org.example.controller;

import org.example.entity.VoiceRecording;
import org.example.service.VoiceRecordingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/recordings")
public class VoiceRecordingController {

    private static final Logger log = LoggerFactory.getLogger(VoiceRecordingController.class);

    private final VoiceRecordingService voiceRecordingService;

    public VoiceRecordingController(VoiceRecordingService voiceRecordingService) {
        this.voiceRecordingService = voiceRecordingService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<VoiceRecording> uploadRecording(@RequestParam("file") MultipartFile file) {
        log.info("Received upload request for file: {} (size: {} bytes)", file.getOriginalFilename(), file.getSize());

        if (file.isEmpty()) {
            log.warn("Empty file uploaded");
            return ResponseEntity.badRequest().build();
        }

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        boolean isAudio = (contentType != null && contentType.startsWith("audio/")) ||
                         (filename != null && (filename.toLowerCase().endsWith(".wav") || 
                                              filename.toLowerCase().endsWith(".mp3") ||
                                              filename.toLowerCase().endsWith(".m4a") ||
                                              filename.toLowerCase().endsWith(".ogg") ||
                                              filename.toLowerCase().endsWith(".flac")));
        if (!isAudio) {
            log.warn("Invalid content type: {} for file: {}", contentType, filename);
            return ResponseEntity.badRequest().build();
        }

        try {
            VoiceRecording recording = voiceRecordingService.processVoiceRecording(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(recording);
        } catch (Exception e) {
            log.error("Error processing recording", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<VoiceRecording>> getAllRecordings() {
        List<VoiceRecording> recordings = (List<VoiceRecording>) voiceRecordingService.getAllRecordings();
        return ResponseEntity.ok(recordings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VoiceRecording> getRecordingById(@PathVariable Long id) {
        VoiceRecording recording = voiceRecordingService.getRecordingById(id);
        if (recording == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(recording);
    }

    @GetMapping("/partner/{businessPartner}")
    public ResponseEntity<List<VoiceRecording>> getRecordingsByBusinessPartner(@PathVariable String businessPartner) {
        List<VoiceRecording> recordings = (List<VoiceRecording>) voiceRecordingService.getRecordingsByBusinessPartner(businessPartner);
        return ResponseEntity.ok(recordings);
    }
}