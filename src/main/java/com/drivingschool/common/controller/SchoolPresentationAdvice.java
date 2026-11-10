// Module owner: IT26101135 - Shared school contact settings and public display text.
package com.drivingschool.common.controller;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class SchoolPresentationAdvice {
    private final Map<String, String> contact;

    public SchoolPresentationAdvice(@Value("${app.school.email:}") String email,
                                    @Value("${app.school.phone:}") String phone,
                                    @Value("${app.school.address:}") String address,
                                    @Value("${app.school.opening-hours:}") String hours) {
        contact = Map.of("email", email.trim(), "phone", phone.trim(),
                "address", address.trim(), "hours", hours.trim());
    }

    @ModelAttribute("schoolContact")
    public Map<String, String> contact() { return contact; }

    @ModelAttribute("publicDisplay")
    public PublicDisplay display() { return new PublicDisplay(); }

    public static class PublicDisplay {
        public String money(BigDecimal amount) {
            if (amount == null) return "-";
            return new DecimalFormat("#,##0.##", DecimalFormatSymbols.getInstance(Locale.UK)).format(amount);
        }

        public String date(Object value) {
            if (value == null) return "Not recorded";
            String text = value.toString();
            if (!text.matches("\\d{4}-\\d{2}-\\d{2}")) return text;
            return LocalDate.parse(text).format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.UK));
        }

        public String time(LocalTime time) {
            return time == null ? "Not recorded" : time.format(DateTimeFormatter.ofPattern("h:mm a", Locale.UK)).toLowerCase(Locale.UK);
        }

        public String rating(Number value) {
            if (value == null || !Double.isFinite(value.doubleValue())) return "0.0";
            return String.format(Locale.UK, "%.1f", value.doubleValue());
        }

        public String label(Object value) {
            if (value == null) return "-";
            return switch (value.toString()) {
                case "MANUAL" -> "Manual car";
                case "AUTOMATIC" -> "Automatic car";
                case "MOTORCYCLE" -> "Motorcycle";
                case "REFRESHER" -> "Refresher";
                case "PENDING" -> "Waiting for confirmation";
                case "AWAITING_PAYMENT" -> "Awaiting payment";
                case "NO_SHOW" -> "No-show";
                case "REJECTED" -> "Declined";
                case "IN_SERVICE" -> "In service";
                case "MAINTENANCE" -> "In maintenance";
                case "OUT_OF_SERVICE" -> "Out of service";
                case "APPROVED" -> "Published";
                case "HIDDEN" -> "Hidden";
                case "STUDENT" -> "Student";
                case "INSTRUCTOR" -> "Instructor";
                case "ADMIN" -> "Administrator";
                case "PAID" -> "Paid";
                case "ACTIVE" -> "Active";
                case "SUSPENDED" -> "Suspended";
                case "INACTIVE" -> "Inactive";
                case "CONFIRMED" -> "Confirmed";
                case "COMPLETED" -> "Completed";
                case "CANCELLED" -> "Cancelled";
                case "CAR" -> "Car";
                case "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY" ->
                        value.toString().substring(0, 1) + value.toString().substring(1).toLowerCase(Locale.UK);
                default -> value.toString().contains("_") ? value.toString().toLowerCase(Locale.UK).replace('_', ' ') : value.toString();
            };
        }

        public String lessonTypeInput(String value) {
            if (value == null) return "";
            return switch (value) {
                case "MANUAL", "AUTOMATIC", "MOTORCYCLE", "REFRESHER" -> label(value);
                default -> value;
            };
        }
    }
}
