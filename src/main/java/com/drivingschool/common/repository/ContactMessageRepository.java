// Module owner: IT26101135
package com.drivingschool.common.repository;

import com.drivingschool.common.model.ContactMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;

@Repository
public class ContactMessageRepository extends FileRepository<ContactMessage> {
    public ContactMessageRepository(@Value("${app.data-directory:./data}") String directory,
                                    ObjectMapper mapper) {
        super(Path.of(directory), "contact-messages.txt", ContactMessage.class, mapper);
    }
}
