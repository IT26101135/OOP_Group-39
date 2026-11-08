# Project Report Draft — DriveSpark Academy

## Introduction
DriveSpark Academy is a Java 17/Spring Boot 3 MVC web application for a driving school. It supports public course discovery, student enrollment and lesson requests, instructor scheduling and lesson records, and administrative operations. The existing MVC/JSP/file-storage structure was extended rather than replaced.

## Roles and module features

- **Visitor:** Home/About/FAQ/Contact, active package details, instructor list with sort options, approved reviews, sign-up/login and demo password reset.
- **Student — IT26101135:** registration with Pending status, login/logout, account status checks, profile/password management, admin approval/suspension, progress and enrollment fields. Students can enroll once at a time and review their own completed bookings.
- **Packages/shared UI — IT26101136:** active/inactive package CRUD, type/duration/price/lesson count, simulated paid enrollment, public JSP pages and role-based navigation.
- **Instructors — IT26101133:** profile data, schedule/leave, dashboard, attendance, lesson notes and skill ratings; public sorting uses the hand-written `InstructorSorter`.
- **Bookings — IT26101134:** future slot generation, server revalidation, reusable conflict checker, one-hour lessons, 24-hour cancel/reschedule cutoff, linked-node FIFO, instructor accept/decline, admin processing/reassignment/force-cancel.
- **Vehicles/sample data — IT26101132:** vehicle CRUD, status, insurance/service dates and warning badges; opt-in repeat-safe demo fixtures.
- **Reviews/reports — IT26101137:** one review per completed booking, moderation, approved-rating recalculation, instructor review view, admin report tables and CSV export.

## OOP and SOLID examples
Models encapsulate their state and enforce validation. `Student`, `Admin`, and `InstructorAccount` specialize `User`; overridden role/dashboard methods demonstrate polymorphism. `Identifiable`, `ReviewStore`, and `VehicleStore` are abstractions; services and repositories separate responsibilities. `BookingConflictChecker` centralizes scheduling rules. These are concrete SOLID examples, but this coursework application is not a claim that every class perfectly satisfies every principle.

## Storage and entities
`FileRepository<T>` stores JSON Lines records in `app.data-directory`. Main records: `User`/`Student`, `Instructor`, `Availability`, `InstructorLeave`, `LearningPackage`, `Enrollment`, `Vehicle`, `Booking`, `LessonRecord`, and `Review`. IDs link the files (for example, `Booking.studentId` and `Booking.enrollmentId`; `Review.bookingId` and `Review.instructorId`). Writes are local file updates with selected in-process locks, not cross-file database transactions.

## Algorithms and data structures

### Bubble sort
`InstructorSorter.sort` compares adjacent instructors and swaps them in nested loops; a pass with no swaps exits early. It supports name, experience and average rating in either direction. Worst/average time is O(n²); best case is O(n) for already ordered values. The returned copy uses O(n) space.

### FIFO queue
`BookingRequestQueue` is a manually linked queue with `head`, `tail`, and `size`. `enqueue`, `dequeue`, `peek`, `isEmpty`, and `size` are O(1); position search is O(n). It filters out cancelled/assigned items when rebuilt and orders pending requests by submission time using insertion sort (O(n²) queue construction). The queue itself uses O(n) space.

## Seed-data arithmetic check
The opt-in loader creates these deterministic totals when run against an empty data directory; the automated idempotency/report test checks these source records:

- Enrollments: 7, all Paid. Prices: LKR 12,500 + 15,000 + 17,500 + 20,000 + 22,500 + 25,000 + 12,500 = **LKR 125,000.00**; Unpaid = LKR 0.00.
- Bookings: Pending 5 + Confirmed 2 + Completed 5 + No-show 1 + Cancelled 1 + Rejected 1 = **15**.
- Reviews: Approved 5, Pending 2, Hidden 3 = **10**. Each approved sample review links a different completed booking and instructor.
- Vehicle usage counts include historical assigned bookings: V001=2, V002=2, V003=1, V004=1, V005=2, V006=1 (total 9 assigned bookings).
- Each of the five instructors receives one approved seed rating; ratings are 2, 4, 1, 3 and 5 respectively. Hidden/Pending reviews do not contribute.

## Verification
The project has JUnit tests for the bubble sort keys/directions and edge cases, FIFO operations/order, conflict reasons, the 24-hour boundary, rating recalculation and repeat-safe sample seeding/report arithmetic. Run `mvnw.cmd clean verify` on Windows. Final verification on 2026-11-07: `mvnw.cmd clean verify` completed successfully; 504 tests ran with 0 failures, 0 errors and 0 skipped. This result is automated test evidence, not a substitute for the manual browser scenarios. Refresh it after future code changes. Manual browser checklist rows remain unrun until the team records them.

## Screenshots
- [Placeholder: visitor home and package list]
- [Placeholder: student available-slot form and progress]
- [Placeholder: instructor dashboard and availability]
- [Placeholder: admin schedule, vehicle warnings and reports]

## Limitations and future work
Payments are simulated as Paid; password-reset tokens are displayed locally rather than emailed. Storage has no database transactions or multi-process locking. Report charts, pagination, activity logging, FAQ/announcement editing, and comprehensive URL-by-URL manual role testing remain future/manual work. The seed runner is intended for a clean local demo directory; do not use its published demo password in a deployed environment.
