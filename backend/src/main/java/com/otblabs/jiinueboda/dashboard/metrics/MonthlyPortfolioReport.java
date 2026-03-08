package com.otblabs.jiinueboda.dashboard.metrics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.YearMonth;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyPortfolioReport {
    private List<MonthlyMetrics> monthlyMetrics;
    private YearMonth oldestMonth;
    private YearMonth newestMonth;
}
