// MEMBER: IT26101137 - Immutable business table shared by CSV and print.
package com.drivingschool.report.service;

import java.util.List;

public record BusinessReportSection(String key, String title, List<String> headings, List<List<String>> rows) {
    public BusinessReportSection {
        headings = List.copyOf(headings);
        rows = rows.stream().map(List::copyOf).toList();
    }
    public String getKey() { return key; }
    public String getTitle() { return title; }
    public List<String> getHeadings() { return headings; }
    public List<List<String>> getRows() { return rows; }
}
