// Module owner: IT26101133
package com.drivingschool.instructor.model;

import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.common.model.Identifiable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class LessonRecord implements Identifiable {
    private final String id;
    private final String bookingId;
    private String notes;
    private Map<String, Integer> skillRatings;
    private boolean readyForTest;
    private LocalDateTime updatedAt;

    @JsonCreator
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

    @Override public String getId() { return id; }
    public String getBookingId() { return bookingId; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) {
        if (notes == null || notes.isBlank() || notes.trim().length() > 2000) {
            throw new ValidationException("Lesson notes must contain 1 to 2000 characters.");
        }
        this.notes = notes.trim();
    }
    public Map<String, Integer> getSkillRatings() { return Map.copyOf(skillRatings); }
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
    public boolean isReadyForTest() { return readyForTest; }
    public void setReadyForTest(boolean readyForTest) { this.readyForTest = readyForTest; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        if (updatedAt == null) throw new ValidationException("Update time is required.");
        this.updatedAt = updatedAt;
    }
}
