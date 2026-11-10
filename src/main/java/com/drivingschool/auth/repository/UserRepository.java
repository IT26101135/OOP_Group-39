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
import com.drivingschool.student.model.Student;
import com.drivingschool.learningpackage.model.Enrollment;
import com.drivingschool.learningpackage.repository.EnrollmentRepository;
import com.drivingschool.common.repository.FileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Repository
// SOLID: S - This class is responsible for saving and reading files.
// OOP: INHERITANCE - The account drawer reuses FileRepository storage rules.
// SOLID: L - Overridden operations preserve every validation and storage check.
public class UserRepository extends FileRepository<User> {
    private final EnrollmentRepository enrollments;

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
        enrollments = new EnrollmentRepository(dataDirectory, objectMapper);
    }
    // CRUD: CREATE - adds a new student account and saves it
    @Override
    // Passes a new account to the shared safe file writer.
    public void save(User account) { super.save(account); }

    // CRUD: READ   - finds or lists student accounts
    @Override
    // Reads all accounts, refusing damaged rows before making safety decisions.
    public List<User> readAllStrict() {
        List<User> accounts = super.readAllStrict();
        for (User account : accounts) clearUnpaidBalance(account);
        return accounts;
    }

    // Checks the linked receipt before sharing a student's saved lesson balance.
    // CRUD: READ   - finds or lists student accounts
    @Override
    public Optional<User> findByIdStrict(String id) {
        Optional<User> account = super.findByIdStrict(id);
        if (account.isPresent()) clearUnpaidBalance(account.get());
        return account;
    }

    // Keeps the older list API consistent with the strict account reads.
    // CRUD: READ   - finds or lists student accounts
    @Override
    public List<User> readAll() {
        List<User> accounts = super.readAll();
        for (User account : accounts) clearUnpaidBalance(account);
        return accounts;
    }

    // Keeps the older lookup API from displaying an unpaid lesson balance.
    // CRUD: READ   - finds or lists student accounts
    @Override
    public Optional<User> findById(String id) {
        Optional<User> account = super.findById(id);
        if (account.isPresent()) clearUnpaidBalance(account.get());
        return account;
    }

    // Treats an unpaid or cancelled request as a receipt, never as usable lessons.
    private void clearUnpaidBalance(User account) {
        if (!(account instanceof Student)) return;
        Student student = (Student) account;
        // Step 1: Only a matching paid receipt can support the saved lesson balance.
        if (hasPaidReceipt(student)) return;
        // Step 2: Clear this loaded copy; reading never rewrites the student's file.
        student.setLessonsRemaining(0);
        student.setActivePackageId(null);
        student.setActiveEnrollmentId(null);
    }

    // Keeps old-format paid receipts valid while refusing missing, unpaid or unrelated receipts.
    private boolean hasPaidReceipt(Student student) {
        if (student.getActiveEnrollmentId() == null) return false;
        Optional<Enrollment> found = enrollments.findByIdStrict(student.getActiveEnrollmentId());
        if (found.isEmpty()) return false;
        Enrollment enrollment = found.get();
        return enrollment.isPaid() && enrollment.getStudentId().equals(student.getId())
                && enrollment.getPackageId().equals(student.getActivePackageId());
    }

    // CRUD: UPDATE - changes an existing student account
    @Override
    // Saves valid changes while preserving the identity of the existing record.
    public void update(User account) { super.update(account); }

    // CRUD: DELETE - removes a student account
    @Override
    // Checks the operation rules before removing the selected record.
    public void delete(String accountId) { super.delete(accountId); }
}
