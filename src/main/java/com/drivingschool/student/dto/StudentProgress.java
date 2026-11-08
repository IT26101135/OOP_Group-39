// Module owner: IT26101135
package com.drivingschool.student.dto;

import com.drivingschool.instructor.model.LessonRecord;
import java.util.List;

public final class StudentProgress {
    private final int lessonsCompleted;
    private final double hoursDriven;
    private final List<LessonRecord> lessonRecords;

    public StudentProgress(int lessonsCompleted, double hoursDriven,
                           List<LessonRecord> lessonRecords) {
        this.lessonsCompleted = lessonsCompleted;
        this.hoursDriven = hoursDriven;
        this.lessonRecords = List.copyOf(lessonRecords);
    }
    public int getLessonsCompleted() { return lessonsCompleted; }
    public double getHoursDriven() { return hoursDriven; }
    public List<LessonRecord> getLessonRecords() { return lessonRecords; }
}
