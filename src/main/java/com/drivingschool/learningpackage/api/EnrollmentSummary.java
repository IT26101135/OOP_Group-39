package com.drivingschool.learningpackage.api;

import java.time.LocalDateTime;

public record EnrollmentSummary(
        String id,
        String studentId,
        String packageId,
        int purchasedLessonAllowance,
        LocalDateTime validFrom,
        LocalDateTime validUntil
) {
}