package com.journalapp.journalservice.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * A journal entry, stored in the "journal_entries" collection. Unlike the monolith (where entries
 * were embedded in the User via @DBRef), each entry now records its owner's username directly -
 * this is what keeps journal-service independent of user-service.
 */
@Document(collection = "journal_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntry {

    @Id
    @JsonSerialize(using = ToStringSerializer.class) // serialize ObjectId as its 24-char hex string
    private ObjectId id;

    @Indexed
    private String userName;   // owner

    private String title;
    private String content;
    private LocalDateTime date;
}
