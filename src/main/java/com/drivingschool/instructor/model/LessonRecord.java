/**
============================================================
MEMBER   : IT26101133 - [Name]
MODULE   : Instructors and bubble sort
FILE     : LessonRecord.java
WHAT IT DOES (simple words): Keeps the details and validation rules of lesson record.
REAL-LIFE EXAMPLE: A teacher diary keeps teaching hours, lesson notes and profile details organised.
OOP USED : Encapsulation (private fields and public methods)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/
package com.drivingschool.instructor.model;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.common.model.Identifiable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

// OOP: ENCAPSULATION - Private fields protect details; constructors and methods validate changes.
public class LessonRecord implements Identifiable {
    private final String id;
    private final String bookingId;
    private String notes;
    private Map<String, Integer> skillRatings;
    private boolean readyForTest;
    private LocalDateTime updatedAt;

    @JsonCreator
    // Receives the helpers or validates the details needed to create this object.
    public LessonRecord(@JsonProperty("id") String id,
                        @JsonProperty("bookingId") String bookingId,
                        @JsonProperty("notes") String notes,
                        @JsonProperty("skillRatings") Map<String, Integer> skillRatings,
                        @JsonProperty("readyForTest") boolean readyForTest,
                        @JsonProperty("updatedAt") LocalDateTime updatedAt) {
        if (id == null || !id.matches("N(?!000)[0-9]{3}")) throw new ValidationException("Lesson note ID is invalid.");
        if (bookingId == null || !bookingId.matches("B(?!000)[0-9]{3}")) throw new ValidationException("Booking ID is invalid.");
        this.id = id;
        this.bookingId = bookingId;
        setNotes(notes);
        setSkillRatings(skillRatings);
        this.readyForTest = readyForTest;
        if (updatedAt == null) throw new ValidationException("Update time is required.");
        this.updatedAt = updatedAt;
    }

    // Lets callers read the id without exposing the private field.
    @Override public String getId() { return id; }
    // Lets callers read the booking id without exposing the private field.
    public String getBookingId() { return bookingId; }
    // Lets callers read the notes without exposing the private field.
    public String getNotes() { return notes; }
    // Changes the notes through the record's validation rules.
    public void setNotes(String notes) {
        if (notes == null || notes.isBlank() || notes.trim().length() > 2000) {
            throw new ValidationException("Lesson notes must contain 1 to 2000 characters.");
        }
        this.notes = notes.trim();
    }
    public Map<String, Integer> getSkillRatings() { return Map.copyOf(skillRatings); }
    // Changes the skill ratings through the record's validation rules.
    public void setSkillRatings(Map<String, Integer> skillRatings) {
        Map<String, Integer> checked = new LinkedHashMap<>();
        if (skillRatings != null) {
            for (Map.Entry<String, Integer> rating : skillRatings.entrySet()) {
                if (rating.getKey() == null || rating.getKey().isBlank()
                        || rating.getValue() == null || rating.getValue() < 1 || rating.getValue() > 5) {
                    throw new ValidationException("Skill ratings must be from 1 to 5.");
                }
                checked.put(rating.getKey().trim(), rating.getValue());
            }
        }
        this.skillRatings = checked;
    }
    // Keeps the is ready for test step in one place for this module.
    public boolean isReadyForTest() { return readyForTest; }
    // Changes the ready for test through the record's validation rules.
    public void setReadyForTest(boolean readyForTest) { this.readyForTest = readyForTest; }
    // Lets callers read the updated at without exposing the private field.
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    // Changes the updated at through the record's validation rules.
    public void setUpdatedAt(LocalDateTime updatedAt) {
        if (updatedAt == null) throw new ValidationException("Update time is required.");
        this.updatedAt = updatedAt;
    }
}
