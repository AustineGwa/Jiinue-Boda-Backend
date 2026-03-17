package com.otblabs.jiinueboda.dashboard.metrics;

import com.otblabs.jiinueboda.loans.models.PendingLoanData;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LoanPortfolioAnalytics {

    /**
     * Calculate month-by-month portfolio metrics
     *
     * @param allLoans List of all loan data
     * @return MonthlyPortfolioReport containing metrics for each month
     */
    public MonthlyPortfolioReport calculateMonthlyMetrics(List<PendingLoanData> allLoans) {
        if (allLoans == null || allLoans.isEmpty()) {
            return new MonthlyPortfolioReport(new ArrayList<>(), null, null);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Group loans by disbursement month
        Map<YearMonth, List<PendingLoanData>> loansByMonth = allLoans.stream()
                .filter(loan -> loan.getDisburseDate() != null && !loan.getDisburseDate().isEmpty())
                .collect(Collectors.groupingBy(loan -> {
                    try {
                        LocalDate date = LocalDate.parse(loan.getDisburseDate(), formatter);
                        return YearMonth.from(date);
                    } catch (Exception e) {
                        return YearMonth.now();
                    }
                }));

        // Sort months chronologically
        List<YearMonth> sortedMonths = loansByMonth.keySet().stream()
                .sorted()
                .toList();

        if (sortedMonths.isEmpty()) {
            return new MonthlyPortfolioReport(new ArrayList<>(), null, null);
        }

        List<MonthlyMetrics> allMonthlyMetrics = new ArrayList<>();
        MonthlyMetrics previousMonthMetrics = null;

        // Calculate metrics for each month
        for (YearMonth month : sortedMonths) {
            List<PendingLoanData> monthLoans = loansByMonth.get(month);

            // Get all loans up to and including this month for cumulative client analysis
            List<PendingLoanData> loansUpToMonth = allLoans.stream()
                    .filter(loan -> {
                        try {
                            if (loan.getDisburseDate() == null || loan.getDisburseDate().isEmpty()) {
                                return false;
                            }
                            LocalDate date = LocalDate.parse(loan.getDisburseDate(), formatter);
                            YearMonth loanMonth = YearMonth.from(date);
                            return !loanMonth.isAfter(month);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());

            MonthlyMetrics metrics = calculateMetricsForMonth(
                    month,
                    monthLoans,
                    loansUpToMonth,
                    previousMonthMetrics
            );

            allMonthlyMetrics.add(metrics);
            previousMonthMetrics = metrics;
        }

        return new MonthlyPortfolioReport(
                allMonthlyMetrics,
                sortedMonths.get(0),
                sortedMonths.get(sortedMonths.size() - 1)
        );
    }

    /**
     * Calculate metrics for a specific month
     */
    private MonthlyMetrics calculateMetricsForMonth(
            YearMonth month,
            List<PendingLoanData> monthLoans,
            List<PendingLoanData> loansUpToMonth,
            MonthlyMetrics previousMonthMetrics) {

        MonthlyMetrics metrics = new MonthlyMetrics();
        metrics.setMonth(month);

        // 1. Number of loans disbursed in this month
        metrics.setNumberOfLoansDisbursed(monthLoans.size());

        // 2. Total principal disbursed in this month
        double totalPrincipal = monthLoans.stream()
                .mapToDouble(PendingLoanData::getPrincipal)
                .sum();
        metrics.setTotalPrincipalDisbursed(totalPrincipal);

        // 3. Loan growth rate (MoM%) - compare with previous month
        if (previousMonthMetrics != null) {
            double previousPrincipal = previousMonthMetrics.getTotalPrincipalDisbursed();
            if (previousPrincipal > 0) {
                double growthRate = ((totalPrincipal - previousPrincipal) / previousPrincipal) * 100.0;
                metrics.setLoanGrowthRateMoM(growthRate);
            } else {
                metrics.setLoanGrowthRateMoM(totalPrincipal > 0 ? 100.0 : 0.0);
            }
        } else {
            metrics.setLoanGrowthRateMoM(0.0); // First month, no comparison
        }

        // 4. Average loan size per client in this month
        Set<Integer> monthUniqueClients = monthLoans.stream()
                .map(PendingLoanData::getUserId)
                .collect(Collectors.toSet());

        double avgLoanSize = monthUniqueClients.size() > 0
                ? totalPrincipal / monthUniqueClients.size()
                : 0.0;
        metrics.setAverageLoanSizePerClient(avgLoanSize);

        // CLIENT ENGAGEMENT & RETENTION (based on cumulative data up to this month)

        // Get all unique clients up to this month
        Set<Integer> allClientsUpToMonth = loansUpToMonth.stream()
                .map(PendingLoanData::getUserId)
                .collect(Collectors.toSet());
        metrics.setTotalUniqueClients(allClientsUpToMonth.size());

        // 5. Number of active clients (totalPaid >= totalExpected)
        Set<Integer> activeClientIds = loansUpToMonth.stream()
                .filter(loan ->  loan.getLoanBalance() > 0)
                .map(PendingLoanData::getUserId)
                .collect(Collectors.toSet());
        metrics.setNumberOfActiveClients(activeClientIds.size());

        // 6. Repeat loan uptake (clients with more than one loan up to this month)
        Map<Integer, Long> loanCountPerUser = loansUpToMonth.stream()
                .collect(Collectors.groupingBy(
                        PendingLoanData::getUserId,
                        Collectors.counting()
                ));

        int repeatLoans = (int) loanCountPerUser.values().stream()
                .filter(count -> count > 1)
                .count();
        metrics.setRepeatLoanCount(repeatLoans);

        // 7. Client retention rate (active vs. drop-offs)
        int droppedOff = allClientsUpToMonth.size() - activeClientIds.size();
        metrics.setDroppedOffClients(droppedOff);

        double retentionRate = allClientsUpToMonth.size() > 0
                ? (activeClientIds.size() * 100.0) / allClientsUpToMonth.size()
                : 0.0;
        metrics.setClientRetentionRate(retentionRate);

        return metrics;
    }

    /**
     * Get metrics for a specific month
     */
    public MonthlyMetrics getMetricsForMonth(List<PendingLoanData> allLoans, YearMonth targetMonth) {
        MonthlyPortfolioReport report = calculateMonthlyMetrics(allLoans);
        return report.getMonthlyMetrics().stream()
                .filter(m -> m.getMonth().equals(targetMonth))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get metrics for current month
     */
    public MonthlyMetrics getCurrentMonthMetrics(List<PendingLoanData> allLoans) {
        return getMetricsForMonth(allLoans, YearMonth.now());
    }

    /**
     * Print comprehensive monthly report
     */
    public void printMonthlyReport(MonthlyPortfolioReport report) {
        if (report.getMonthlyMetrics().isEmpty()) {
            System.out.println("No data available for report.");
            return;
        }

        System.out.println("\n" + "=".repeat(80));
        System.out.println("MONTHLY LOAN PORTFOLIO PERFORMANCE & CLIENT ENGAGEMENT REPORT");
        System.out.println("Period: " + report.getOldestMonth() + " to " + report.getNewestMonth());
        System.out.println("=".repeat(80));

        for (MonthlyMetrics metrics : report.getMonthlyMetrics()) {
            printMonthMetrics(metrics);
        }
    }

    /**
     * Print metrics for a single month
     */
    public void printMonthMetrics(MonthlyMetrics metrics) {
        System.out.println("\n" + "-".repeat(80));
        System.out.printf("MONTH: %s%n", metrics.getMonth());
        System.out.println("-".repeat(80));

        System.out.println("\nLOAN PORTFOLIO PERFORMANCE:");
        System.out.printf("  • Loans Disbursed: %d%n", metrics.getNumberOfLoansDisbursed());
        System.out.printf("  • Total Principal Disbursed: KES %.2f%n", metrics.getTotalPrincipalDisbursed());
        System.out.printf("  • Loan Growth Rate (MoM): %.2f%%%n", metrics.getLoanGrowthRateMoM());
        System.out.printf("  • Average Loan Size per Client: KES %.2f%n", metrics.getAverageLoanSizePerClient());

        System.out.println("\nCLIENT ENGAGEMENT & RETENTION:");
        System.out.printf("  • Active Clients: %d%n", metrics.getNumberOfActiveClients());
        System.out.printf("  • Total Unique Clients: %d%n", metrics.getTotalUniqueClients());
        System.out.printf("  • Repeat Loan Clients: %d%n", metrics.getRepeatLoanCount());
        System.out.printf("  • Client Retention Rate: %.2f%%%n", metrics.getClientRetentionRate());
        System.out.printf("  • Dropped-off Clients: %d%n", metrics.getDroppedOffClients());
    }

    /**
     * Print summary comparison between two months
     */
    public void printMonthComparison(MonthlyMetrics month1, MonthlyMetrics month2) {
        System.out.println("\n" + "=".repeat(80));
        System.out.printf("COMPARISON: %s vs %s%n", month1.getMonth(), month2.getMonth());
        System.out.println("=".repeat(80));

        double principalChange = month2.getTotalPrincipalDisbursed() - month1.getTotalPrincipalDisbursed();
        int loanCountChange = month2.getNumberOfLoansDisbursed() - month1.getNumberOfLoansDisbursed();
        int clientChange = month2.getTotalUniqueClients() - month1.getTotalUniqueClients();

        System.out.printf("Principal Change: KES %.2f (%.2f%%)%n",
                principalChange,
                month1.getTotalPrincipalDisbursed() > 0 ?
                        (principalChange / month1.getTotalPrincipalDisbursed()) * 100 : 0);
        System.out.printf("Loan Count Change: %d%n", loanCountChange);
        System.out.printf("Client Change: %d%n", clientChange);
        System.out.printf("Retention Rate Change: %.2f percentage points%n",
                month2.getClientRetentionRate() - month1.getClientRetentionRate());
    }
}