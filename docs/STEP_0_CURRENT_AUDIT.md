# Step 0: current-state audit, 10 November 2026

**Review only. No application source, CSS, tests, README or existing checklist was
edited.** The new files are this report and audit evidence. No Git write commands
were used. Existing working-tree changes were retained.

The current checkout differs from the historical business review: completion gates
and commitment limits already exist. This report distinguishes current observations
from the older findings. No implementation batch has started.

## Build evidence

The DriveSpark application on port 8080, PID 30904, was identified by its main class
and stopped before building. Java 17 was selected in the build shell. Actual command:
`.\mvnw.cmd clean verify`.

```text
[INFO] Tests run: 653, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Total time:  03:33 min
[INFO] Finished at: 2026-11-10T10:38:55+05:30
```

The Surefire XML totals independently agree: 653 tests, zero failures/errors/skips.
**Changed tests: none.** CSS was not rebuilt in this read-only step; the checked-in
generated CSS was used. [Saved build summary](screenshots/step0-current/verify-summary.txt).
Full build log: `%TEMP%\drivespark-step0-verify.log`.

## Your two reported issues

**Login appearance:** reproduced. Login and registration have zero horizontal card
padding, so their text and controls touch the rounded card edges. Labels on login,
registration and password reset are white on a white background: measured contrast
**1.00:1**. An older dark-form rule overrides the newer white-form rule.

**Only one person/package on normal startup:** the settings explain this. Normal
startup uses `app.data-directory=./data` and both seed flags default to false. The
existing `data` folder currently contains three accounts, one instructor and one
package. Your command enables the optional loader and changes the directory, so
it correctly creates six instructors and seven packages in a new folder.

The IntelliJ default launch was not repeated against that existing data, because
this audit must run only on fresh empty folders. The configuration and existing
record counts were inspected read-only. **UNVERIFIED:** the IDE's actual Run
Configuration environment. Proposed fix: a clearly documented isolated preview
launcher and IDE instructions; keep automatic seeding disabled for ordinary data.
Do not delete, replace or seed over your existing `data` folder.

## Runtime setup and screenshot coverage

Two fresh PowerShell processes ran the newly verified WAR on localhost:18080.
Each set only its own `DRIVESPARK_DEMO_SEED_ENABLED=true`,
`DRIVESPARK_ADMIN_SEED_ENABLED=false` and `APP_DATA_DIRECTORY` variables.
The attendance override was **not** set; the default true was exercised.

- Initial screenshot/probe folder:
  `%TEMP%\drivespark-step0-21c0b5a0eaeb4c35a5c73614deb1d63e`.
- Clean smoke repeat folder:
  `%TEMP%\drivespark-step0-5bd38d0e650e4ff880b64a9a8f856b22`.

Both directories were newly created and empty before startup. All mutations,
including a working-hours deletion used to reproduce a bug, affected these
fictional records only. No live application actions were clicked or submitted.

[Screenshot gallery and complete route/role matrix](screenshots/step0-current/index.html).
There are **49 routes x four roles = 196 primary screenshots at 1536 x 864**,
plus 94 full-page captures and additional error/empty-state screenshots. Public
pages were visited under every role. Protected pages were also visited under
the wrong roles; the gallery records the actual rendered destination and status.
Expected login redirects and access-denied pages are not counted as successful
access to protected content.

Coverage includes home, about, FAQ, contact, packages and all seven package details,
instructors and sorting, public reviews, login, registration, password reset;
student dashboard/profile edit/password/progress/enrollments/payment/bookings/
booking details/reviews; instructor dashboard and availability; administrator
dashboard/students/instructors/packages/enrollments/bookings/vehicles/create/all
six vehicle-edit forms/reviews/reports; setup and missing-record/page responses.

The primary matrix returned 134 HTTP 200 responses, 52 HTTP 403 responses and ten
HTTP 404 responses, including expected role and missing-record checks. No capture
errors, missing images, detected garbled text or horizontal overflow were found in
that matrix. Additional probes deliberately produced HTTP 400/401 and one 500.

## Combined findings and proposed fixes

High = wrong result, crash or unreadable information. Medium = edge case, confusing
flow or visibly poor layout. Low = polish. Owners follow source module headers;
shared integration work may involve a second member. **These are proposals only.**

Java paths below start at `src/main/java/com/drivingschool/`; JSP paths start at
`src/main/webapp/WEB-INF/views/`; CSS means `src/main/resources/static/css/app.css`.

| ID | Area | Role | Problem and evidence | Severity | Owner member | Proposed fix / batch | Files | Risk |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| F01 | Startup and seed selection | All / developer | Your normal run reads the old `./data`: one instructor and one package. The new fixtures are opt-in and stored elsewhere. Configuration diagnosis confirmed; IDE environment UNVERIFIED. | Medium | IT26101132 / IT26101135 | Include an isolated preview launcher and clear IDE/startup instructions in A or B. Always create a fresh folder; preserve the safe disabled seed default. | `application.properties`; `sample/DemoDataSeedRunner.java` (inspect only unless needed); `README.md`; proposed `scripts/run-demo.ps1` | Low if preview-only; high if existing data is overwritten, which is excluded. |
| F02 | Attendance | Instructor | No-show is allowed at the lesson start, rather than after its end. Code compares `now` with `startTime`; the passing model test explicitly permits exact start. Before-start runtime refusal passed. During-lesson browser timing UNVERIFIED. | High | IT26101134 | A: compare against end time for both attendance outcomes; retain default-true setting and explicit demo override. Keep existing completion protection. | `booking/model/Booking.java`; `booking/service/BookingService.java`; attendance tests; README/viva/checklist | Medium: changes the no-show boundary and its messages. |
| F03 | Renewal | Student / admin | Expired S006/E006 has no open lesson but cannot renew: redirected with "You already have an active paid package." Used-up S007 can request a package because its fixture already cleared the active link. Code also blocks exhausted accounts retaining that link; payment confirmation has the same blanket check. | High | IT26101136 / IT26101135 | A: share a renewal-eligibility rule at request and payment confirmation. Allow expired/exhausted accounts only with no open bookings; retain historical receipts and one awaiting request. Test the retained-link exhausted case explicitly. | `learningpackage/service/EnrollmentService.java` (`ensureCanEnroll`, `paymentStudent`, activation); enrollment tests | High: payment/balance/history consistency. |
| F04 | Working-hours deletion | Instructor / student | Removing H028 succeeded while B004 remained Confirmed. Updating hours has protection; deleting does not. Runtime reproduced. | High | IT26101133 | A: apply the confirmed-lesson protection to deletion, and coordinate schedule writes with booking writes using the existing app lock. | `instructor/service/InstructorScheduleService.java`; scheduling tests | Medium: legitimate schedule changes must still work. |
| F05 | Leave and pending assignments | Instructor / admin | Leave over Sayumi's assigned Pending B003 was saved without warning; booking remained Pending and becomes impossible to accept. Runtime reproduced. | High | IT26101133 / IT26101134 | A: block conflicting assigned Pending bookings with a clear instruction to ask an administrator to resolve them. Preserve the confirmed-lesson block. | `instructor/service/InstructorScheduleService.java`; scheduling tests and messages | Medium: distinguish assigned from unassigned pending requests. |
| F06 | Package validity and lesson date | Student / admin | E001 expires 25 Dec 2026, but a request for 28 Dec 2026 at 2:00 pm was saved. Current guard checks today's date only. Runtime reproduced. | High | IT26101134 / IT26101136 | Add to A if approved: validate requested lesson date against receipt validity during request, reschedule, assignment and acceptance. Preserve inclusive expiry-day policy and legacy null-validity handling. | `booking/service/PaidEnrollmentGuard.java`; `BookingService.java`; conflict/slot checks and tests | Medium: existing future appointments need a clear policy. |
| F07 | Invalid schedule filter | Admin | `/admin/bookings?status=BOGUS` returns 500. Server evidence identifies JSP EL coercion of the string into `BookingStatus`; no stack trace is exposed in the browser. | Medium | IT26101134 | B: validate status before rendering and compare safe string values in the select. Show a clear validation message with a return-to-filter link. | `booking/controller/AdminBookingController.java`; `booking/service/BookingService.java`; `admin/bookings.jsp` | Low: preserve valid filters and existing parameter names. |
| F08 | Contact acknowledgement | Visitor | Submission says "Keep an eye on your email for our reply." Only a file record is saved; no email is sent and no staff inbox exists. Runtime acknowledgement and source path confirmed. | Medium | IT26101135 | B: use truthful confirmation and establish a usable staff retrieval/contact path. An inbox would be a separate feature choice; do not invent contact details. | `public/contact.jsp`; `common/service/ContactService.java`; `PublicPagesController.java`; README | Medium: actual staff workflow/contact configuration needs a decision. |
| F09 | Empty booking flow | New / unpaid student | A student without a package is told to choose an available time "above", but the time form is hidden. The overview says "Not recorded lessons" and presents many unavailable statistics. Runtime empty-state captures. | Medium | IT26101134 / IT26101136 | B: conditional next-step text; link directly to packages/payment as appropriate. Show a compact no-package state instead of meaningless statistics. | `booking/list.jsp`; `common/components/enrollment-usage.jspf`; `learningpackage/service/EnrollmentPresentationService.java` | Low: display only; keep payment gate. |
| F10 | Error wording | Instructor / admin | Repeating completion for completed B006 shows "Only CONFIRMED bookings can be completed". Raw enum and mechanical wording remain on error paths. Runtime reproduced; other model strings need the same targeted audit. | Medium | IT26101134 | B: explain the state in plain words and give the next step; retain status codes and transition rules. | `booking/model/Booking.java`; related validation messages; `common/error.jsp` only if a recovery link is missing | Low: update only tests asserting changed copy. |
| V01 | Authentication labels | Visitor | Login: two invisible labels; registration: five; reset: five. Computed foreground and background are both white, contrast 1.00:1. This is the unreadable part of your login report. | High | IT26101135 | C, or an explicitly approved urgent visual fix: repair shared auth selector precedence once; use dark labels on white. | CSS; `auth/login.jsp`, `register.jsp`, `forgot-password.jsp` only if needed | Low: scope overrides to auth forms so dark marketing stays intact. |
| V02 | Authentication alignment | Visitor | Login/registration headings, fields and links touch the card's left/right edges; measured horizontal padding is 0px. Detached photo and card lack a balanced shared container. | Medium | IT26101135 | C: shared responsive form/photo layout with 20-24px inner padding and consistent gaps; keep field names and routes. | CSS; auth JSP wrappers if needed | Low: check all three desktop sizes and a narrow phone. |
| V03 | Overview card alignment | Student / admin | Student's course card starts about 20px below the lessons card. Admin payment-attention card touches the next row of stat cards. Screenshots show inconsistent spacing. | Medium | IT26101135 / IT26101136 | C: remove carried block margins inside shared grids; use one parent gap and align cards at the top. | CSS; `student/profile.jsp`; `admin/dashboard.jsp` | Low: layout only; avoid changing totals. |
| V04 | Repeated active sidebar links | Instructor | Both Dashboard and Assigned lessons appear active on the same page. They lead to the same route/anchor, so the current location is unclear. | Low | IT26101133 / IT26101135 | C: choose one active page item and style in-page anchors separately. | `common/dashboard-sidebar.jspf`; CSS | Low: retain both navigation destinations. |
| V05 | Technical text in profile input | Student | The editable Lesson type field shows `MANUAL` although the profile read view says "Manual car". Input values are not covered by an inner-text-only scan. Runtime confirmed. | Medium | IT26101135 | B/C: use a friendly display value and normalize on save where needed; preserve `licenseTypeWanted` and supported values. | `student/profile-edit.jsp`; profile form/presentation mapping | Medium: avoid turning presentation text into an incompatible stored value. |
| V06 | Unavailable attendance controls | Instructor / admin | Future Confirmed lessons still offer enabled completion/no-show buttons. Completion then leads to a full validation page. Runtime action-state capture. | Medium | IT26101133 / IT26101134 | C after A: show when attendance becomes available and disable/hide unavailable actions, respecting the demo override; server remains authoritative. | `instructor/lesson-dashboard.jsp`; `admin/booking-card.jspf`; controller view data | Medium: UI and server clocks/settings must agree. |
| V07 | Rating precision | Visitor / instructor | After approving R013, the average is displayed as `4.33 / 5`, while reports use one decimal. The calculation is correct; display precision varies. | Low | IT26101137 / IT26101133 | C: format displayed averages consistently, without changing stored review ratings or sorting. | `public/instructors.jsp`; instructor rating display; report formatting stays intact | Low: formatting only. |
| V08 | Dense reference fields in empty states | New student | No-package overview repeats unavailable/not-recorded labels; unpaid overview uses "Not recorded" for lessons although the package has a known lesson count. Links exist, but the distinction between course allowance and usable credit is unclear. | Low | IT26101136 | B/C with F09: say "Lessons available after payment" and separate allowance from usable balance. Preserve current no-credit-before-payment behavior. | `common/components/enrollment-usage.jspf`; `EnrollmentPresentationService.java` | Low: presentation only. |

Evidence: [functional probes](screenshots/step0-current/functional-probes.json),
[additional probes](screenshots/step0-current/additional-audit.json),
[error/action states](screenshots/step0-current/state-checks.json),
[contrast readings](screenshots/step0-current/contrast-pages.json).

## Smoke checklist: every original step

The original checklist uses old package names, prices, dates and IDs. This run uses
the current fixtures: a new Audit Student S013, P001 at LKR 25,000 with ten lessons,
E010, and new request B025 on **24 Nov 2026, 2:00 pm-3:00 pm**. Fixture password is
unchanged. Phone `+94000000999` and reserved `.test` email are fictional audit data.

PASS means the functional browser/HTTP/storage assertion succeeded. Browser login,
navigation and screenshot capture used Chrome; write probes used its authenticated
request context with actual CSRF tokens. PASS does not mean every button was clicked
or that its visual appearance passed. **UNVERIFIED** means that the exact requested
flow could not be demonstrated, even if related automated tests passed.

| Step | Check | Result | Current evidence |
| ---: | --- | --- | --- |
| 1 | Visitor home | PASS | HTTP 200; school brand displayed as DRIVESPARK ACADEMY. |
| 2 | Packages | PASS | HTTP 200; current named courses, including Manual Car: Beginner Course. |
| 3 | Instructors | PASS | HTTP 200; six named instructors. |
| 4 | Public reviews | PASS | HTTP 200; published reviews displayed. |
| 5 | Register | PASS | S013 saved; redirect to `/login?registered`. |
| 6 | New student login | PASS | Redirected to `/student/profile`. |
| 7 | Enroll in P001 | PASS | E010 saved Awaiting payment; payment-page redirect. |
| 8 | Payment instructions | PASS | HTTP 200; ENR-E010, LKR 25,000 and payment next steps. Contact lines intentionally hidden because configured values are empty. |
| 9 | Attempt unpaid booking | PASS | HTTP 400; no booking saved. |
| 10 | Admin dashboard | PASS | HTTP 200; awaiting-payment section present. |
| 11 | Confirm payment | PASS | E010 Paid; ten credits granted once. |
| 12 | Student enrollment status | PASS | Paid shown on `/enrollments`. |
| 13 | Find free hour | PASS | Available inputs include 24 Nov 2026 at 2:00 pm. |
| 14 | Submit request | PASS | B025 saved Pending. |
| 15 | Numeric queue position | PASS | B025 displays "Position in waiting list 3" before assignment. |
| 16 | Assign maintenance vehicle | PASS | V003 rejected with HTTP 400; B001 remains unassigned. |
| 17 | Assign FIFO heads then B025 | PASS | B001 and B002 handled first; B025 assigned I001/V001 and remains Pending. |
| 18 | Student sees instructor-acceptance stage | PASS | Own B025 page says "Assigned - waiting for instructor approval." |
| 19 | Instructor sees assigned learner | PASS | Instructor1 dashboard contains Audit Student. |
| 20 | Instructor accepts | PASS | B025 becomes Confirmed. |
| 21 | Reject future completion | PASS | HTTP 400; lesson end-time explanation; B025 remains Confirmed, balance stays ten. The old failure is not current. |
| 22 | Debit after successful completion | UNVERIFIED | Correct gate blocks this future lesson, so no successful browser completion/debit can be observed in this chain. The ended-lesson integration test passes separately. |
| 23 | Count this newly completed lesson in progress | UNVERIFIED | B025 is not completed; progress page returns 200 with zero completed lessons and 0.0 hours. Seeded progress pages were captured separately. |
| 24 | Create review for this completed lesson | UNVERIFIED | B025 is not completed. Negative probe correctly returns 400 and saves no review. Successful new-review creation in this chain was not exercised. |
| 25 | Approve pending review | PASS | Substitution explicitly recorded: existing R013/B018, rather than the blocked new review, changes from Pending to Approved. |
| 26 | Recalculate/display rating | PASS | R013 approval changes I001 from 4.5 to 4.33, matching approved-review arithmetic and public display. |
| 27 | Reports | PASS | HTTP 200; current business sections shown. |
| 28 | CSV export | PASS | HTTP 200; UTF-8 BOM, business section title and dated filename; downloaded bytes retained. |
| 29 | Student opens admin dashboard | PASS | HTTP 403; recovery links point to student profile and home. |
| 30 | Wrong password | PASS | HTTP 401; login error, no authenticated dashboard. |
| 31 | Suspended student login | PASS | Student10 gets HTTP 401 with suspension explanation. |
| 32 | Cancel within 24 hours | PASS | Separate same-day 4:00 pm request B026; cancellation returns 400 and leaves Pending unchanged. |
| 33 | Setup endpoint | PASS | HTTP 200 with website-available message. This endpoint is a basic page, not storage/health diagnostics. |

**Total: 33 steps; 30 PASS, 0 FAIL, 3 UNVERIFIED.** Additional probes below found
real defects outside those 33 checks. This total is not a claim that the app has no
bugs. [Complete smoke results](screenshots/step0-current/smoke-results.json).

Initial probe assertions were corrected and rerun against fresh data: the queue
label is now "Position in waiting list", R013's actual rating produces 4.33 rather
than the probe's assumed 4.0, and error assertions must inspect the message rather
than the first 700 characters of HTML. A final safe home GET corrected a
case-sensitive school-brand assertion. These were harness mistakes, not app fixes;
the initial probe results are retained separately for transparency.

## Additional checks and historical findings

- Both instructor and administrator future completion return 400 without changing
  Confirmed status. No-show before the lesson starts is also rejected. The remaining
  defect is the no-show boundary during the lesson (F02).
- Ten remaining credits permit exactly ten open Pending/Confirmed requests; the next
  two ordinary attempts return 400. Existing passing tests also cover replacement
  at the limit, over-cap assignment/reschedule, and counting Confirmed lessons.
  No blanket cap rewrite is proposed.
- A repeated payment confirmation does not add credits again. A duplicate same-slot
  booking returns 400 and leaves exactly one matching record. Existing locking and
  payment rules are retained.
- Duplicate awaiting-payment request for Student8 is refused without a new receipt.
  Renewal for expired Student6 fails; used-up Student7 with an already-cleared active
  link succeeds. Both distinctions are recorded in F03.
- Malformed filter date returns a friendly 400 with a return-to-form link. Malformed
  filter status returns 500 (F07). Missing vehicle returns a friendly 404.
- Current role-denied pages have the correct role-specific recovery link. No JSON
  error or browser-visible stack trace was observed in these HTML requests. These
  historical concerns are not declared current bugs without evidence.
- Empty enrollments link to Browse packages. Empty progress links to bookings.
  Empty booking guidance and unavailable statistics still need F09/V08.
- The raw-ID text scan found only intended payment references such as ENR-E001 and
  ENR-E008 on normal account pages; these references are needed for manual payment.
  Raw enum wording remains in the error and editable-field cases listed above.
- The inherited multi-file persistence limit remains: completion and balance debit
  are separate file writes with no database transaction. Fault injection/recovery
  was not exercised in this audit. Do not promise atomic rollback; this is a known
  storage limit, not a verified new crash. No new persistence framework is proposed.

## Suggested implementation sequence, pending approval

1. **Batch A:** keep existing completion setting and cap protections; correct the
   no-show end boundary, renewal eligibility and schedule deletion/leave protections.
   Add F06's expiry-date check only if included in your approval. Clarify startup
   selection/provide a safe preview helper as your reported functional setup issue.
2. **Batch B:** fix the reproduced filter error and misleading/dead-end messages;
   finish the targeted validation/empty-state audit. Agree the contact retrieval
   behavior before adding any staff inbox feature.
3. **Batch C:** repair the unreadable auth labels first, then auth alignment and
   shared spacing/navigation/formatting. Verify at all three requested desktop sizes
   and check for obvious phone breakage. If you want the High auth-contrast issue
   moved ahead of other work, approve that explicitly with A.

After each approved batch: stop the preview before building, build CSS, run full
`clean verify`, list each changed test with its reason, launch on a fresh empty
folder, repeat affected smoke steps and update README/checklist/viva notes only
where behavior changed. Stop for review after that batch.

## Limits

**UNVERIFIED:** every possible record/filter/form-error combination; all six
instructor logins and all twelve student logins (representative roles plus the
specific scenario accounts were used); desktop Excel; physical printing; mobile
and the other two desktop sizes (reserved for C); all hover/focus/assistive-technology
states; arbitrary JSON/API Accept headers; actual IDE environment; simulated
mid-write storage corruption; actual wall-clock passage through a lesson end.

Contrast scanning covered the 20 selected authenticated pages and the three auth
forms on solid backgrounds. It found the twelve invisible auth labels. It does
not certify text over photographs or the full WCAG standard. Full-page captures
supplement the requested 1536 x 864 viewport evidence; they are labelled separately.

Both isolated application processes are stopped at completion of Step 0, their
run-only demo variables are cleared, and no DriveSpark listener is left on 8080 or
18080. Existing source/test/README/checklist hashes were checked against the
pre-audit snapshot before handing back for approval.
