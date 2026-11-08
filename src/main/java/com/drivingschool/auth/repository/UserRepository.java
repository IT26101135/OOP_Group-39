/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : UserRepository.java
WHAT IT DOES (simple words): Keeps user records in files.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.auth.repository;

import com.drivingschool.auth.model.User;
import com.drivingschool.common.repository.FileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.List;

@Repository
// SOLID: S - This class is responsible for saving and reading files.
// OOP: INHERITANCE - The account drawer reuses FileRepository storage rules.
// SOLID: L - Overridden operations preserve every validation and storage check.
public class UserRepository extends FileRepository<User> {

    // Receives the helpers this class needs so each job stays separate.
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
    // CRUD: CREATE - adds a new student account and saves it
    @Override
    // Passes a new account to the shared safe file writer.
    public void save(User account) { super.save(account); }

    // CRUD: READ   - finds or lists student accounts
    @Override
    // Reads all accounts, refusing damaged rows before making safety decisions.
    public List<User> readAllStrict() { return super.readAllStrict(); }

    // CRUD: UPDATE - changes an existing student account
    @Override
    // Saves valid changes while preserving the identity of the existing record.
    public void update(User account) { super.update(account); }

    // CRUD: DELETE - removes a student account
    @Override
    // Checks the operation rules before removing the selected record.
    public void delete(String accountId) { super.delete(accountId); }
}