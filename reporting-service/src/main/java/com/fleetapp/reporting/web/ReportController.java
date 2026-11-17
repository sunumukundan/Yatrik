package com.fleetapp.reporting.web;

import com.fleetapp.reporting.service.ReportingService;
import com.fleetapp.reporting.service.ReportingService.FuelSpendReportResponse;
import com.fleetapp.reporting.service.ReportingService.ReceivableRowResponse;
import com.fleetapp.reporting.web.dto.FinancialReportResponse;
import com.fleetapp.reporting.web.dto.UtilizationReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * The dashboard's reports.
 *
 * Every report takes a from/to window and defaults to the current month.
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportingService reportingService;

    @GetMapping("/utilization")
    public UtilizationReportResponse utilization(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportingService.utilization(start(from), end(to));
    }

    @GetMapping("/financials")
    public FinancialReportResponse financials(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportingService.financials(start(from), end(to));
    }

    @GetMapping("/fuel-spend")
    public FuelSpendReportResponse fuelSpend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportingService.fuelSpend(start(from), end(to));
    }

    @GetMapping("/receivables")
    public List<ReceivableRowResponse> receivables() {
        return reportingService.receivables();
    }

    /** Defaults to the first day of the current month. */
    private LocalDate start(LocalDate from) {
        return from != null ? from : LocalDate.now().withDayOfMonth(1);
    }

    /** Defaults to today. */
    private LocalDate end(LocalDate to) {
        return to != null ? to : LocalDate.now();
    }
}
