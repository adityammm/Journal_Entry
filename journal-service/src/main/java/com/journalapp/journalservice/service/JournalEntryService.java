package com.journalapp.journalservice.service;

import com.journalapp.journalservice.dto.JournalEntryRequest;
import com.journalapp.journalservice.entity.JournalEntry;
import com.journalapp.journalservice.event.JournalEventProducer;
import com.journalapp.journalservice.repository.JournalEntryRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalEventProducer journalEventProducer;

    public JournalEntryService(JournalEntryRepository journalEntryRepository,
                               JournalEventProducer journalEventProducer) {
        this.journalEntryRepository = journalEntryRepository;
        this.journalEventProducer = journalEventProducer;
    }

    /** Creates an entry owned by {@code userName}, then publishes JOURNAL_CREATED. */
    public JournalEntry create(JournalEntryRequest request, String userName) {
        JournalEntry entry = new JournalEntry();
        entry.setUserName(userName);
        entry.setTitle(request.getTitle());
        entry.setContent(request.getContent());
        entry.setDate(LocalDateTime.now());

        JournalEntry saved = journalEntryRepository.save(entry);
        journalEventProducer.publishJournalCreated(saved);
        return saved;
    }

    public List<JournalEntry> getAllForUser(String userName) {
        return journalEntryRepository.findByUserName(userName);
    }

    /** ADMIN view: every entry in the system. */
    public List<JournalEntry> getAll() {
        return journalEntryRepository.findAll();
    }

    public Optional<JournalEntry> getByIdForUser(ObjectId id, String userName) {
        return journalEntryRepository.findByIdAndUserName(id, userName);
    }

    public Optional<JournalEntry> update(ObjectId id, String userName, JournalEntryRequest request) {
        Optional<JournalEntry> existing = journalEntryRepository.findByIdAndUserName(id, userName);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        JournalEntry entry = existing.get();
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            entry.setTitle(request.getTitle());
        }
        if (request.getContent() != null && !request.getContent().isBlank()) {
            entry.setContent(request.getContent());
        }
        return Optional.of(journalEntryRepository.save(entry));
    }

    public boolean delete(ObjectId id, String userName) {
        if (!journalEntryRepository.existsByIdAndUserName(id, userName)) {
            return false;
        }
        journalEntryRepository.deleteByIdAndUserName(id, userName);
        return true;
    }
}
