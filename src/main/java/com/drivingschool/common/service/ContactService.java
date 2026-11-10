// Module owner: IT26101135
package com.drivingschool.common.service;

import com.drivingschool.common.model.ContactMessage;
import com.drivingschool.common.repository.ContactMessageRepository;
import com.drivingschool.auth.service.AuthSessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class ContactService {
    private final ContactMessageRepository messages;
    private final Clock clock;
    private final AuthSessionService auth;

    public ContactService(ContactMessageRepository messages, Clock clock, AuthSessionService auth) {
        this.messages = messages;
        this.clock = clock;
        this.auth = auth;
    }

    public synchronized void submit(String name, String email, String message) {
        ContactMessage validated = new ContactMessage("M001", name, email,
                message, LocalDateTime.now(clock));
        // A quick retry or double click must not give staff two copies of the same message.
        for (ContactMessage saved : messages.readAllStrict()) {
            if (saved.getName().equals(validated.getName()) && saved.getEmail().equals(validated.getEmail())
                    && saved.getMessage().equals(validated.getMessage())
                    && !saved.getCreatedAt().isBefore(validated.getCreatedAt().minusMinutes(1))
                    && !saved.getCreatedAt().isAfter(validated.getCreatedAt())) return;
        }
        String id = messages.nextId("M");
        messages.save(new ContactMessage(id, validated.getName(), validated.getEmail(),
                validated.getMessage(), validated.getCreatedAt()));
    }

    // CRUD: READ - staff read saved enquiries on the existing protected dashboard.
    public List<ContactMessage> messagesForAdmin(HttpServletRequest request) {
        auth.requireAdmin(request);
        List<ContactMessage> saved = new ArrayList<>(messages.readAllStrict());
        saved.sort(Comparator.comparing(ContactMessage::getCreatedAt).reversed());
        return saved;
    }
}
