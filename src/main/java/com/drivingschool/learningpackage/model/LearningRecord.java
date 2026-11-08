/**
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and enrollment
FILE     : LearningRecord.java
WHAT IT DOES (simple words): Keeps the details and rules of learning record.
REAL-LIFE EXAMPLE: A course folder keeps the learning plan and enrollment details together.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.learningpackage.model;

import com.drivingschool.common.model.Identifiable;

// SOLID: I - Shared record headings need only an ID and a readable title.
// OOP: ABSTRACTION - Pages depend on this promise rather than file details.
// SOLID: I - This contract exposes only operations for one kind of caller.
// SOLID: O - Another learning record can supply its own heading without changing callers.
public interface LearningRecord extends Identifiable {
    // Gives pages a readable heading for either kind of learning record.
    String getDisplayName();
}
