package com.journalapp.journalservice.repository;

import com.journalapp.journalservice.entity.JournalEntry;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface JournalEntryRepository extends MongoRepository<JournalEntry, ObjectId> {
    List<JournalEntry> findByUserName(String userName);
    Optional<JournalEntry> findByIdAndUserName(ObjectId id, String userName);
    boolean existsByIdAndUserName(ObjectId id, String userName);
    void deleteByIdAndUserName(ObjectId id, String userName);
}
