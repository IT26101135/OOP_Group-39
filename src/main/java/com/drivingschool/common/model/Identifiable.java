/**
 * ============================================================
 * MEMBER   : IT26101135 - [Name]
 * MODULE   : Shared foundation
 * FILE     : Identifiable.java
 * WHAT IT DOES (simple words): Promises that a saved record can tell us its ID.
 * REAL-LIFE EXAMPLE: Every folder in the cabinet has a file number.
 * OOP USED : Abstraction (one small interface)
 * CRUD     : READ (gets a record ID)
 * ============================================================
 */
package com.drivingschool.common.model;

/**
 * Shared contract for records that have an ID.
 */
// SOLID: I - Saving only needs this small ID contract.
public interface Identifiable {

    // Gives the record number used to find the right saved folder.
    String getId();
}