package com.journalapp.journalservice.controller;

import com.journalapp.journalservice.dto.JournalEntryRequest;
import com.journalapp.journalservice.entity.JournalEntry;
import com.journalapp.journalservice.service.JournalEntryService;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/journal")
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    public JournalEntryController(JournalEntryService journalEntryService) {
        this.journalEntryService = journalEntryService;
    }

    private String currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @GetMapping
    public ResponseEntity<?> getAllOfUser() {
        List<JournalEntry> entries = journalEntryService.getAllForUser(currentUser());
        if (entries.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(entries, HttpStatus.OK);
    }

    /** ADMIN only (enforced by SecurityConfig): read every entry. */
    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        List<JournalEntry> entries = journalEntryService.getAll();
        if (entries.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(entries, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody JournalEntryRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        JournalEntry saved = journalEntryService.create(request, currentUser());
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getById(@PathVariable ObjectId id) {
        Optional<JournalEntry> entry = journalEntryService.getByIdForUser(id, currentUser());
        return entry.map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/id/{id}")
    public ResponseEntity<?> update(@PathVariable ObjectId id, @RequestBody JournalEntryRequest request) {
        Optional<JournalEntry> updated = journalEntryService.update(id, currentUser(), request);
        return updated.map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<?> delete(@PathVariable ObjectId id) {
        boolean removed = journalEntryService.delete(id, currentUser());
        return new ResponseEntity<>(removed ? HttpStatus.NO_CONTENT : HttpStatus.NOT_FOUND);
    }
}
