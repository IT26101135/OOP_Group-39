/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : StudentProfiles.java
WHAT IT DOES (simple words): Defines the small set of operations another class promises to provide.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.student.api;

import com.drivingschool.student.dto.StudentProfile;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;

// SOLID: I - Pages only need to read and edit their own profile.
// OOP: ABSTRACTION - Callers use this small promise instead of implementation details.
// SOLID: I - Only the operations needed by these callers are exposed.
public interface StudentProfiles {
    // Returns only the signed-in student details that the page is allowed to show.
    StudentProfile getOwnProfile(HttpServletRequest request);
    // Validates a replacement profile before saving so invalid changes leave old details safe.
    StudentProfile updateOwnProfile(HttpServletRequest request, String name,
            String email, String phone, String nic, LocalDate dateOfBirth,
            String address, String licenseTypeWanted, String emergencyContact);
}
