package org.example.repository;

import org.example.entity.VoiceRecording;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoiceRecordingRepository extends JpaRepository<VoiceRecording, Long> {
    List<VoiceRecording> findByBusinessPartner(String businessPartner);
    List<VoiceRecording> findByBusinessPartnerContainingIgnoreCase(String businessPartner);
}