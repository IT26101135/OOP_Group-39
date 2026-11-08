// Module owner: IT26101135
package com.drivingschool.common.service;

import com.drivingschool.common.model.ContactMessage;
import com.drivingschool.common.repository.ContactMessageRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class ContactService {
    private final ContactMessageRepository messages;
    private final Clock clock;

    public ContactService(ContactMessageRepository messages, Clock clock) {
        this.messages = messages;
        this.clock = clock;
    }

    public void submit(String name, String email, String message) {
        ContactMessage validated = new ContactMessage("M001", name, email,
                message, LocalDateTime.now(clock));
        String id = messages.nextId("M");
        messages.save(new ContactMessage(id, validated.getName(), validated.getEmail(),
                validated.getMessage(), validated.getCreatedAt()));
    }
}
