# Driving School System Test Checklist

Automated tests run with `.\mvnw.cmd clean verify` on Windows. Manual browser scenarios are marked Not run until a member records actual behavior. Use a separate empty `APP_DATA_DIRECTORY` for seeding.

| ID | Module | Scenario | Steps | Expected result | Actual result | Pass/fail |
|---|---|---|---|---|---|---|
| A01 | Auth | Valid/invalid sign-up | Submit valid and malformed email, phone, NIC and password values | Valid user is BCrypt-hashed and Pending; invalid input is rejected | Automated validation tests; browser pass not recorded | Manual not run |
| A02 | Auth | Duplicate email | Submit same address twice | Second registration is rejected | Automated suite | Pass |
| A03 | Auth | Pending/suspended login | Login with seeded students 8/10 | Clear waiting/suspended message, no authenticated session | Manual not run | Manual not run |
| A04 | Auth | Role redirects and CSRF | Login each role; submit logout without/with token | Correct role route; write without CSRF is blocked | Automated auth tests | Pass |
| A05 | Student | Profile/password/admin status | Edit profile, change password, approve/suspend | Own data changes; status gates login | Automated profile/admin tests; full manual flow not recorded | Partial |
| B01 | Packages | CRUD and validation | Add/edit/deactivate package; try nonpositive price/lessons | Valid package persists; invalid values reject | Existing package tests | Pass |
| B02 | Enrollment | Active package limit | Enroll and attempt second active enrollment | Lesson balance set and second active enrollment rejected | Manual browser flow not recorded | Not run |
| B03 | Public UI | Visitor pages | Browse Home/About/Packages/Instructors/FAQ/Contact/Reviews | Public pages show active packages/approved reviews | Manual browser flow not recorded | Not run |
| C01 | Instructor | Bubble sort | Test name/experience/rating both directions, empty, one item and ties | Adjacent swaps produce stable expected output | JUnit `InstructorSorterTest` | Pass |
| C02 | Instructor | Schedule and leave | Add valid/overlapping availability and leave | Invalid overlaps rejected; leave blocks slots | Manual browser flow not recorded | Not run |
| C03 | Instructor | Accept/attendance/progress | Accept/decline, complete/no-show, enter skill notes | Status and lesson balance/progress update | Conflict/cutoff unit tests; manual flow not recorded | Partial |
| D01 | Booking | Slot generation | Select instructor/date with availability and leave | Only future, unoccupied one-hour slots appear; empty state otherwise | `SlotAvailabilityService` compile; browser scenario not run | Not run |
| D02 | Booking | Server-side conflict checks | Attempt past, leave, nonworking, instructor/student/vehicle overlap, inactive package, wrong vehicle type/status | Request rejected with specific reason; rejected reason visible to student | JUnit conflict checker tests | Pass |
| D03 | Booking | FIFO and cancellation | Submit several requests, cancel one, reschedule one, process next | Oldest remaining pending request processes; reschedule joins queue tail | JUnit queue tests; browser scenario not run | Partial |
| D04 | Booking | 24-hour boundary | Cancel at 23:59 and exactly 24 hours; reschedule | Inside cutoff rejected; exactly 24 hours allowed; replacement is new Pending | JUnit cutoff tests; reschedule UI not manually run | Partial |
| D05 | Admin schedule | Filters/reassign/force-cancel | Filter date/instructor/vehicle/status, reassign and force-cancel | Conflict rechecked; reason stored; confirmation shown | Manual browser flow not recorded | Not run |
| E01 | Vehicle | CRUD/status/date warnings | Edit plate/type/status/dates; set service/insurance expired or near | Duplicate/invalid dates rejected; warning badge appears; unavailable vehicle blocked | Vehicle JUnit/web integration tests | Pass |
| E02 | Sample seed | Repeat seed | Start twice with demo seed enabled and same clean data path | 1 admin, 5 instructors, 10 students, 6 packages, 6 vehicles, 7 enrollments, 15 bookings, 10 reviews; no duplicate records | `DemoDataSeedRunnerTest` checks counts and rerun | Pass |
| F01 | Reviews | Eligibility and uniqueness | Review pending, non-owned, incomplete and completed bookings; duplicate completed review | Only own Completed booking gets one 1â€“5 review | JUnit rating/moderation tests; browser eligibility not recorded | Partial |
| F02 | Reviews | Moderation/rating | Approve, hide, delete and filter reviews | Only Approved reviews public; instructor average recalculated after each change | `InstructorRatingRecalculationTest` | Pass |
| F03 | Reports | Counts and CSV | Compare tables/export against seeded records | Enrollment, status, rating, vehicle and revenue totals match arithmetic in REPORT.md | Seed/report arithmetic JUnit test | Pass |

## Protected URL role matrix

Automated integration tests cover representative anonymous, student and admin requests. This full matrix still needs a recorded manual browser pass. Expected: visitors redirect to `/login?required`; logged-in roles other than the allowed role receive 403.

| Protected URL(s) | Visitor | Student | Instructor | Admin | Manual status |
|---|---|---|---|---|---|
| `/student/profile`, `/student/profile/edit`, `/student/progress`, `/student/password`, `/student/account`, `/enrollments`, `/bookings` | Block | Allow | Block | Block | Not run for every URL |
| `/instructor/dashboard`, `/instructor/availability` | Block | Block | Allow | Block | Not run for every URL |
| `/admin/dashboard`, `/admin/students`, `/admin/instructors`, `/admin/packages`, `/admin/vehicles`, `/admin/bookings`, `/admin/reviews`, `/admin/reports`, `/admin/reports/export.csv` | Block | Block | Block | Allow | Not run for every URL |
| `/reviews` | Block | Own + approved | Own instructor reviews | All/moderation | Not run for every role |
| Public home/about/packages/instructors/FAQ/contact/reviews | Allow | Allow | Allow | Allow | Not run |

Security notes: passwords are BCrypt hashed; server-side validation and role checks are used; JSP content uses `<c:out>` for escaped dynamic output; POST requests are CSRF checked. Reset delivery is simulated and local file storage does not provide database transactions.

## Automated verification record

2026-11-07: mvnw.cmd clean verify — PASS; 504 tests, 0 failures, 0 errors, 0 skipped. This verifies automated tests only; manual browser rows above remain unrun.
