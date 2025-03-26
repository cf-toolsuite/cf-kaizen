package org.cftoolsuite.cfapp.service.ai;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

class MetricUtils {

    /**
     * Calculates tokens per second based on total tokens and response time in milliseconds.
     *
     * @param totalTokens Total number of tokens processed
     * @param responseTimeMs Response time in milliseconds
     * @return Tokens per second, rounded to 2 decimal places
     */
    static Double calculateTokensPerSecond(Integer totalTokens, long responseTimeMs) {
        if (totalTokens == null || totalTokens == 0 || responseTimeMs == 0) {
            return 0.0;
        }

        // Convert milliseconds to seconds (as a decimal)
        double responseTimeSeconds = responseTimeMs / 1000.0;

        // Calculate tokens per second
        double tps = totalTokens / responseTimeSeconds;

        // Round to 2 decimal places
        BigDecimal bd = BigDecimal.valueOf(tps);
        bd = bd.setScale(2, RoundingMode.HALF_UP);

        return bd.doubleValue();
    }

    /**
     * Formats a duration into a human-readable string (e.g., "1m30s")
     */
    static String formatResponseTime(Duration duration) {
        long totalSeconds = duration.getSeconds();
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        if (minutes > 0) {
            return String.format("%dm%ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
}
