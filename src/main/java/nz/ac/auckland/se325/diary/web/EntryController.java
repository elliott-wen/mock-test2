package nz.ac.auckland.se325.diary.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import nz.ac.auckland.se325.diary.model.Entry;
import nz.ac.auckland.se325.diary.model.EntryRequest;
import nz.ac.auckland.se325.diary.model.User;
import nz.ac.auckland.se325.diary.repository.EntryRepository;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/entries")
public class EntryController {

    private final EntryRepository entries;

    public EntryController(EntryRepository entries) {
        this.entries = entries;
    }

    /** GET /api/entries?q=gym */
    @GetMapping
    public List<Entry> list(HttpSession session, @RequestParam(required = false) String q) {
        User me = CurrentUser.require(session);
        return entries.findForUser(me.id(), q == null ? "" : q.trim());
    }

    @GetMapping("/{id}")
    public Entry get(HttpSession session, @PathVariable long id) {
        User me = CurrentUser.require(session);
        return entries.findForUser(me.id(), id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No such entry"));
    }

    @PostMapping
    public ResponseEntity<Entry> create(HttpSession session, @RequestBody EntryRequest request) {
        User me = CurrentUser.require(session);
        if (request.date() == null) {
            throw new IllegalArgumentException("Please pick a date");
        }
        if (request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("Please give the entry a title");
        }
        String body = request.body() == null ? "" : request.body();

        long id = entries.insert(me.id(), request.date(), request.title().trim(), body);
        return ResponseEntity.status(HttpStatus.CREATED).body(entries.findForUser(me.id(), id).orElseThrow());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(HttpSession session, @PathVariable long id) {
        User me = CurrentUser.require(session);
        if (entries.delete(me.id(), id) == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "No such entry");
        }
        return ResponseEntity.noContent().build();
    }
}
