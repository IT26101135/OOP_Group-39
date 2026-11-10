/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : DashboardSummaryProvider.java
WHAT IT DOES (simple words): Applies the rules for dashboard summary provider.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.common.service;

import com.drivingschool.common.model.DashboardSummary;

// SOLID: I - The dashboard needs just one read operation.
public interface DashboardSummaryProvider {
    DashboardSummary readSummary();
}
