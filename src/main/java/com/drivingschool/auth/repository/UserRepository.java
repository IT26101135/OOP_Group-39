package com.drivingschool.auth.repository;

import com.drivingschool.auth.model.User;
import com.drivingschool.common.repository.FileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;

@Repository
public class UserRepository extends FileRepository<User> {

    public UserRepository(
            @Value("${app.data-directory:./data}") String dataDirectory,
            ObjectMapper objectMapper) {

        super(
                Path.of(dataDirectory),
                "users.txt",
                User.class,
                objectMapper
        );
    }
}