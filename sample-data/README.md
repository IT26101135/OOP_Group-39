# SE1020 viva sample data

Owner: **IT26101132 — Vehicles, sample data and test checklist**. Prepared **9 November 2026**, with dates based on that day in Asia/Colombo. This is a synthetic viva/submission bundle, not a backup of `data/` or `data-demo/`.

Every person, qualification, license number, vehicle registration, lesson and review is invented. Account emails use the reserved `.test` domain; phone values are deliberately zero placeholders. NIC, birth date, address and emergency contact are null. BCrypt hashes were generated for new throwaway accounts by the app's opt-in seed in a fresh TEMP directory; no live user file or real password/hash was copied. All accounts use the public demo-only password below.

No existing sample-data folder or unwanted files needed removal. This folder contains only this README, twelve record files and twelve matching counter files (**25 files**). No files were removed. Logs, scripts, screenshots, environment files, reset/session tokens and build artifacts do not belong here. Mention and review unexpected files before removing them.

## Load a disposable copy

Keep this bundle unchanged. Run the app against a fresh TEMP copy, never this submission folder or live `data/`. **Do not enable the seed runner over these imported records:** its numbered fixture relationships differ.

Open a **fresh PowerShell shell**, change to the project root and select Java 17 or newer (`java -version`). Copy records and counters together, then run with both loaders off:

```powershell
$vivaRunDirectory = Join-Path $env:TEMP ("drivespark-viva-" + [guid]::NewGuid())
New-Item -ItemType Directory -Path $vivaRunDirectory | Out-Null
Get-ChildItem -LiteralPath .\sample-data -Filter '*.txt' |
    Copy-Item -Destination $vivaRunDirectory

$env:DRIVESPARK_DEMO_SEED_ENABLED = "false"
$env:DRIVESPARK_ADMIN_SEED_ENABLED = "false"
$env:DRIVESPARK_ATTENDANCE_REQUIRE_LESSON_ENDED = "true"
$env:APP_DATA_DIRECTORY = $vivaRunDirectory
try {
    Write-Output "Temporary viva data: $vivaRunDirectory"
    .\mvnw.cmd spring-boot:run
}
finally {
    Remove-Item Env:DRIVESPARK_DEMO_SEED_ENABLED,
        Env:DRIVESPARK_ADMIN_SEED_ENABLED,
        Env:DRIVESPARK_ATTENDANCE_REQUIRE_LESSON_ENDED,
        Env:APP_DATA_DIRECTORY -ErrorAction SilentlyContinue
}
```

Open http://localhost:8080/. Stop with Ctrl+C before leaving the shell or preparing another run. Repeat with a fresh copy for a clean reset; do not delete or overwrite live records. The temporary copy is retained for inspection.

The default attendance setting refuses completion before lesson end and no-show before lesson start. For an explicitly explained attendance simulation only, change the run's attendance variable to false. Payment, ownership, status and commitment checks remain in force. Clear all run-only variables afterwards as shown.

## Date-sensitive snapshot

These are static, readable records, not automatically refreshed data. Future lessons are **12–15 November 2026**; leave is **16 November 2026**. As those dates pass, available slots/expiry warnings and attendance behavior naturally change. Starting with seeding disabled does not change dates.

For a later viva, refresh the **disposable copy before starting the app**. Shift every ISO date/datetime by the same number of days between 9 November 2026 and the new rehearsal day. This includes account createdAt, receipt enrolledAt/validUntil, booking startTime/endTime/createdAt, leave date, note updatedAt, review createdAt/updatedAt and vehicle insurance/service dates. Preserve null validity, weekly day names, time-only availability, statuses, IDs, links and counters. This keeps expiry yesterday, past completed lessons, future requests and service warnings relative to the new day. Do not shift a copy twice. If dates are not refreshed, use the original dates honestly and do not claim a stale request is a future lesson. No date-refresh source tool is added by this task.

## Demo logins

Every sample account uses **DriveSpark123!**. This is a public throwaway fixture password, not a real person's credential. Users store BCrypt hashes, never plaintext passwords.

| Account | Login | Scenario |
|---|---|---|
|Admin A001|admin@drivingschool.test|Payments, schedule, users, vehicles, moderation, reports/CSV|
|Instructors I001–I005|instructor1@drivingschool.test through instructor5@drivingschool.test|Availability, assigned lessons, attendance and notes|
|Students S001–S005|student1@drivingschool.test through student5@drivingschool.test|Active Paid learning; student4 has legacy null validity|
|Student S006|student6@drivingschool.test|Active login; expired Paid receipt blocks booking despite13 recorded credits|
|Student S007|student7@drivingschool.test|Active login; Used up receipt, nine completed lessons, zero credits, no open booking|
|Student S008|student8@drivingschool.test|Active login; Awaiting payment E008 and Cancelled E009, no usable lessons|
|Student S009|student9@drivingschool.test|Pending account; correct password is refused|
|Student S010|student10@drivingschool.test|Suspended account; correct password is refused|

These records deliberately differ from the normal seed's counts/relationships. Use this bundle's table while rehearsing. **E009 belongs to S008, not S009.**

## Files and matching ID counters

Record files are UTF-8 **JSON Lines**, one object per line, not an array or CSV. Empty files are empty stores. Each `<name>.txt.ids.txt` holds the greatest reserved number per prefix. Copy counters with their record files.

| Record file | Rows | Counter contents | Next ID in an untouched copy |
|---|---:|---|---|
|users.txt|16|`{"A":1,"I":5,"S":10}`|A002 / I006 / S011|
|instructors.txt|5|`{"I":5}`|I006|
|packages.txt|6|`{"P":6}`|P007|
|enrollments.txt|10|`{"E":10}`|E011|
|vehicles.txt|6|`{"V":6}`|V007|
|bookings.txt|21|`{"B":21}`|B022|
|reviews.txt|5|`{"R":5}`|R006|
|availability.txt|35|`{"H":35}`|H036|
|instructor-leave.txt|1|`{"L":1}`|L002|
|lesson-records.txt|3|`{"N":3}`|N004|
|instructor-availability.txt|0|`{}`|T001|
|contact-messages.txt|0|`{}`|M001|
|Total|**108**|12 matching counter files||

The separate legacy instructor-availability store is intentionally empty. Real slot generation uses weekly `availability.txt` and `instructor-leave.txt`. Contact messages start empty; no personal correspondence or email delivery is implied.

## Package links and balances

P001–P006 contain 9, 10, 11, 12, 13, 14 lessons; their types are MANUAL, AUTOMATIC, MOTORCYCLE, REFRESHER, MANUAL, AUTOMATIC. All are catalog-active. Prices are synthetic LKR 12,500 through 25,000 in steps of 2,500.

| Student | Paid receipt/package | Completed on receipt | Account lessons left | Open commitments | Example |
|---|---|---:|---:|---:|---|
|S001|E001/P001|1|8|1 Pending|B001, FIFO position 1; separate historical E010/P002|
|S002|E002/P002|1|9|1 Pending|B002, FIFO position 2|
|S003|E003/P003|0|11|1 assigned Pending|B003 I003/V003 awaiting acceptance; B008 No-show costs no credit|
|S004|E004/P004|0|12|1 Confirmed|B004; null validity displays Validity not recorded|
|S005|E005/P005|0|13|1 Confirmed|B005 I005/V001; B010 separately records rejection|
|S006|E006/P006|1|13|0|Expired 8 November 2026; B011 completed before expiry|
|S007|E007/P001|9|0|0|B012–B020 completed; active package/enrollment pointers null|
|S008|None current Paid|0|0|0|E008 Awaiting payment, E009 Cancelled|
|S009/S010|None|0|0|0|Pending/Suspended login refusal|

S001 also owns old Paid E010/P002 and completed B021, with null legacy validity. E010 does not supply S001's current balance; E001 is current. No historical balance is invented. The app labels the old receipt Historical and explains that its remaining balance is not recorded.

All Pending/Confirmed commitments fit the account balance. Open assignments match package type, working hours, vehicle state and leave. Completed lessons occur after enrollment. Active commitments do not overlap for a student/instructor/vehicle. No deliberately missing enrollment/package reference is included: this submission data is coherent.

### Expected totals and algorithm examples

- Enrollments: **8 Paid, 1 Awaiting payment, 1 Cancelled**. Captured Paid revenue: **LKR140,000**, including old/expired/used-up Paid receipts.
- Bookings: **3 Pending** (2 unassigned +1 assigned), **2 Confirmed**, **13 Completed**, **1 No-show**, **1 Cancelled**, **1 Rejected**. Total 21.
- Reviews: **3 Approved, 1 Pending, 1 Hidden**. Each references its student's completed booking and matching instructor; no duplicate booking reviews.
- Approved averages: **I001=4.5, I002=4.0; I003/I004/I005=0.0**. Pending/Hidden feedback does not contribute.
- Instructor experience in file order: **3,8,1,5,5** years. Descending bubble-sort output: **I002,I004,I005,I001,I003**. Equal-experience I004/I005 keep their order.
- Unassigned FIFO: **B001 then B002**. Assigned Pending B003 is awaiting instructor approval and is outside that queue.
- All instructors have weekly 08:00–17:00 blocks every day. I005 leave L001 is 16 November 2026, with no active booking that day.
- **V003 is a motorcycle. V005 is MAINTENANCE** with no active assignment. V006 is an automatic car with insurance due 19 November 2026 and service scheduled 14 November 2026 for warning badges.
- Notes N001/B006, N002/B007, N003/B020 show completed feedback and skill ratings; N003 marks ready for test.

## Short viva walkthrough

1. Visitor: Packages → Instructors → sort by experience; explain the stable tie.
2. Student 1: dashboard → enrollments → bookings → B001 details; show package/receipt links, current versus historical balance and FIFO position.
3. Admin: show B001/B002 FIFO separately from assigned B003. Process requests or confirm S008 payment only in the disposable copy.
4. Instructor 3: show B003 awaiting acceptance. Instructor 4/5 show confirmed lessons; default time gates prevent premature completion. Instructor 1/2 show past lesson feedback.
5. Students 4/6/7/8: show null validity, expired, used-up and unpaid/cancelled examples. Students 9/10 demonstrate blocked login.
6. Admin: show maintenance/warnings, moderate reviews and compare report/CSV totals with this README. Explain a JSON line and its ID counter.

Actions change only the TEMP copy. Start with another fresh copy to repeat. Do not perform destructive actions on live records.

## Verification performed

On 9 November 2026 the bundle passed **375 structural/link/balance/counter/privacy assertions**: ID uniqueness/formats, exact high-water counters, .test identities, null personal fields, matching instructor accounts, receipt ownership, compatible vehicles, intervals, balances, commitment caps, ratings, review/note links, leave/overlaps and Paid revenue.

The packaged app was started in a fresh shell with both seed variables false and APP_DATA_DIRECTORY pointing to a new TEMP copy of all 24 data/counter files. **45 HTTP assertions passed**: all 16 expected login outcomes, 20 public/role/report responses, experience order, legacy/expired/used-up/unpaid display and revenue. New synthetic registration allocated **S011**, retaining the A/I counter values. A synthetic locally saved contact allocated **M001** from the empty store. These writes occurred only in the disposable validation copy; no emails were sent.

All24 bundle file SHA-256 digests remained unchanged during runtime checks. The app was stopped and its shell printed Demo variables cleared. Scripts/results remained outside this folder under TEMP (viva-sample-validation.json and viva-sample-http-results.json). Browser appearance, a full transactional journey with this exact bundle and a later real-viva date remain **UNVERIFIED**. HTTP checks did exercise real model deserialization and JSP rendering. No new source build or unit-test run is claimed for this data-only task.

## Submission status

`sample-data/` is outside ignored `data/` and `data-demo/`. It is prepared for review, **not committed/pushed**. No Git write command, source edit or file removal was performed. The project lead decides when to include it. Do not add live records or private configuration alongside it.

## Java cleanup (2026-11-09)

Unused instructor interval/lookup, vehicle lookup and enrollment lookup classes were removed. Working CRUD pages, file storage, OOP interfaces, the manual instructor bubble sort and manual booking queue remain. The empty legacy `instructor-availability.txt` sample and its counter are retained for compatibility and are not read by the application. See the [removal and verification record](../docs/JAVA_SIMPLIFICATION.md).
