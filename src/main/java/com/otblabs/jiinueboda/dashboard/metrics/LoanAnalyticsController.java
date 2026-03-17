package com.otblabs.jiinueboda.dashboard.metrics;

import com.otblabs.jiinueboda.loans.LoanManagementService;
import com.otblabs.jiinueboda.loans.models.PendingLoanData;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analytics/metrics")
public class LoanAnalyticsController {


    private final LoanPortfolioAnalytics analytics;
    private final LoanManagementService loanManagementService;

    public LoanAnalyticsController(LoanPortfolioAnalytics analytics, LoanManagementService loanManagementService) {
        this.analytics = analytics;
        this.loanManagementService = loanManagementService;
    }

    /**
     * Get all monthly metrics
     *
     */
    @GetMapping("/monthly")
    public ResponseEntity<MonthlyPortfolioReport> getAllMonthlyMetrics() {
        try {
            List<PendingLoanData> allLoans = getAllSystemLoanBalances();
            MonthlyPortfolioReport report = analytics.calculateMonthlyMetrics(allLoans);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get metrics for current month
     * GET /api/v1/analytics/monthly/current
     */
    @GetMapping("/monthly/current")
    public ResponseEntity<MonthlyMetrics> getCurrentMonthMetrics() {
        try {
            List<PendingLoanData> allLoans = getAllSystemLoanBalances();
            MonthlyMetrics metrics = analytics.getCurrentMonthMetrics(allLoans);

            if (metrics == null) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get metrics for a specific month
     * GET /api/v1/analytics/monthly/2024-06
     *
     * @param yearMonth Month in format YYYY-MM (e.g., 2024-06)
     */
    @GetMapping("/monthly/{yearMonth}")
    public ResponseEntity<MonthlyMetrics> getMetricsForMonth(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {
        try {
            List<PendingLoanData> allLoans = getAllSystemLoanBalances();
            MonthlyMetrics metrics = analytics.getMetricsForMonth(allLoans, yearMonth);

            if (metrics == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get comparison between two months
     * GET /api/v1/analytics/monthly/compare?month1=2024-05&month2=2024-06
     *
     * @param month1 First month in format YYYY-MM
     * @param month2 Second month in format YYYY-MM
     */
    @GetMapping("/monthly/compare")
    public ResponseEntity<Map<String, Object>> compareMonths(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month1,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month2) {
        try {
            List<PendingLoanData> allLoans = getAllSystemLoanBalances();

            MonthlyMetrics metrics1 = analytics.getMetricsForMonth(allLoans, month1);
            MonthlyMetrics metrics2 = analytics.getMetricsForMonth(allLoans, month2);

            if (metrics1 == null || metrics2 == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> comparison = new HashMap<>();
            comparison.put("month1", metrics1);
            comparison.put("month2", metrics2);
            comparison.put("changes", calculateChanges(metrics1, metrics2));

            return ResponseEntity.ok(comparison);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get metrics for a date range
     * GET /api/v1/analytics/monthly/range?startMonth=2024-01&endMonth=2024-06
     *
     * @param startMonth Start month in format YYYY-MM
     * @param endMonth End month in format YYYY-MM
     */
    @GetMapping("/monthly/range")
    public ResponseEntity<List<MonthlyMetrics>> getMetricsForRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth startMonth,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth endMonth) {
        try {
            List<PendingLoanData> allLoans = getAllSystemLoanBalances();
            MonthlyPortfolioReport report = analytics.calculateMonthlyMetrics(allLoans);

            List<MonthlyMetrics> filteredMetrics = report.getMonthlyMetrics().stream()
                    .filter(m -> !m.getMonth().isBefore(startMonth) && !m.getMonth().isAfter(endMonth))
                    .toList();

            if (filteredMetrics.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(filteredMetrics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get summary statistics across all months
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummaryStatistics() {
        try {
            List<PendingLoanData> allLoans = getAllSystemLoanBalances();
            MonthlyPortfolioReport report = analytics.calculateMonthlyMetrics(allLoans);

            if (report.getMonthlyMetrics().isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            Map<String, Object> summary = new HashMap<>();

            // Calculate totals and averages
            double totalPrincipalAllTime = report.getMonthlyMetrics().stream()
                    .mapToDouble(MonthlyMetrics::getTotalPrincipalDisbursed)
                    .sum();

            int totalLoansAllTime = report.getMonthlyMetrics().stream()
                    .mapToInt(MonthlyMetrics::getNumberOfLoansDisbursed)
                    .sum();

            double avgMonthlyPrincipal = totalPrincipalAllTime / report.getMonthlyMetrics().size();

            double avgMonthlyLoans = (double) totalLoansAllTime / report.getMonthlyMetrics().size();

            MonthlyMetrics latestMonth = report.getMonthlyMetrics()
                    .get(report.getMonthlyMetrics().size() - 1);

            summary.put("totalMonths", report.getMonthlyMetrics().size());
            summary.put("oldestMonth", report.getOldestMonth());
            summary.put("newestMonth", report.getNewestMonth());
            summary.put("totalPrincipalAllTime", totalPrincipalAllTime);
            summary.put("totalLoansAllTime", totalLoansAllTime);
            summary.put("averageMonthlyPrincipal", avgMonthlyPrincipal);
            summary.put("averageMonthlyLoans", avgMonthlyLoans);
            summary.put("latestMonthMetrics", latestMonth);

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get year-to-date metrics
     * GET /api/v1/analytics/ytd
     */
    @GetMapping("/ytd")
    public ResponseEntity<Map<String, Object>> getYearToDateMetrics() {
        try {
            List<PendingLoanData> allLoans = getAllSystemLoanBalances();
            YearMonth currentMonth = YearMonth.now();
            YearMonth startOfYear = YearMonth.of(currentMonth.getYear(), 1);

            MonthlyPortfolioReport report = analytics.calculateMonthlyMetrics(allLoans);

            List<MonthlyMetrics> ytdMetrics = report.getMonthlyMetrics().stream()
                    .filter(m -> m.getMonth().getYear() == currentMonth.getYear())
                    .toList();

            if (ytdMetrics.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            Map<String, Object> ytdSummary = new HashMap<>();

            double ytdPrincipal = ytdMetrics.stream()
                    .mapToDouble(MonthlyMetrics::getTotalPrincipalDisbursed)
                    .sum();

            int ytdLoans = ytdMetrics.stream()
                    .mapToInt(MonthlyMetrics::getNumberOfLoansDisbursed)
                    .sum();

            ytdSummary.put("year", currentMonth.getYear());
            ytdSummary.put("ytdPrincipal", ytdPrincipal);
            ytdSummary.put("ytdLoans", ytdLoans);
            ytdSummary.put("monthlyBreakdown", ytdMetrics);

            return ResponseEntity.ok(ytdSummary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Helper method to calculate changes between two months
     */
    private Map<String, Object> calculateChanges(MonthlyMetrics month1, MonthlyMetrics month2) {
        Map<String, Object> changes = new HashMap<>();

        double principalChange = month2.getTotalPrincipalDisbursed() - month1.getTotalPrincipalDisbursed();
        double principalChangePercent = month1.getTotalPrincipalDisbursed() > 0
                ? (principalChange / month1.getTotalPrincipalDisbursed()) * 100
                : 0;

        int loanCountChange = month2.getNumberOfLoansDisbursed() - month1.getNumberOfLoansDisbursed();
        int clientChange = month2.getTotalUniqueClients() - month1.getTotalUniqueClients();
        int activeClientChange = month2.getNumberOfActiveClients() - month1.getNumberOfActiveClients();
        double retentionRateChange = month2.getClientRetentionRate() - month1.getClientRetentionRate();

        changes.put("principalChange", principalChange);
        changes.put("principalChangePercent", principalChangePercent);
        changes.put("loanCountChange", loanCountChange);
        changes.put("clientChange", clientChange);
        changes.put("activeClientChange", activeClientChange);
        changes.put("retentionRateChange", retentionRateChange);

        return changes;
    }


    private List<PendingLoanData> getAllSystemLoanBalances() {
         return loanManagementService.getAllSystemLoanBalances(0);
    }
}