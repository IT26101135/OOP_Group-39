/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : StudentProgress.java
WHAT IT DOES (simple words): Shares safe student progress details with pages.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.student.dto;

import com.drivingschool.instructor.model.LessonRecord;
import java.util.List;

public final class StudentProgress {
    private final int lessonsCompleted;
    private final double hoursDriven;
    private final List<LessonRecord> lessonRecords;

    // Receives the helpers this class needs so each job stays separate.
    public StudentProgress(int lessonsCompleted, double hoursDriven,
                           List<LessonRecord> lessonRecords) {
        this.lessonsCompleted = lessonsCompleted;
        this.hoursDriven = hoursDriven;
        this.lessonRecords = List.copyOf(lessonRecords);
    }
    // Shares the lessons completed through a public method while the field stays private.
    public int getLessonsCompleted() { return lessonsCompleted; }
    // Shares the hours driven through a public method while the field stays private.
    public double getHoursDriven() { return hoursDriven; }
    // Shares the lesson records through a public method while the field stays private.
    public List<LessonRecord> getLessonRecords() { return lessonRecords; }
}
