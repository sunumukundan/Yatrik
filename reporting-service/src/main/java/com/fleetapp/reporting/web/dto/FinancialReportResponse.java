package com.fleetapp.reporting.web.dto;

/**
 * The money picture for a period. All amounts are in paise.
 *
 * The distinction that matters here: customerAdvance is money ALREADY received, so
 * it is not revenue and never counted in profit. Only freightAmount is what the
 * business earned.
 */
public record FinancialReportResponse(
        long totalFreight,
        long totalTripExpenses,
        long totalProfit,
        long customerAdvanceReceived,
        long driverAdvanceGiven,
        long outstandingReceivables,
        long marginPercent
) {
    /**
     * marginPercent = profit / freight * 100. Zero freight means no margin, not an
     * error — a period with no completed trips has no margin to report.
     */
    public static FinancialReportResponse of(long totalFreight, long totalTripExpenses,
                                             long customerAdvanceReceived, long driverAdvanceGiven,
                                             long outstandingReceivables) {
        long profit = totalFreight - totalTripExpenses;
        long margin = totalFreight == 0
                ? 0
                : Math.round((double) profit * 100 / totalFreight);

        return new FinancialReportResponse(
                totalFreight, totalTripExpenses, profit,
                customerAdvanceReceived, driverAdvanceGiven, outstandingReceivables, margin);
    }
}
