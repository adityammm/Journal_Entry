package com.journalapp.journalservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request body for creating/updating a journal entry. The owner comes from the JWT, not the body. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryRequest {
    private String title;
    private String content;
}
