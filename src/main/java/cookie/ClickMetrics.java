package cookie;

import cookie.exceptions.IncompleteMetricsException;

public class ClickMetrics {

    private long startTime; // in ms
    private long endTime; // in ms
    private double timeDelta; // in s
    private long startTotal;
    private long endTotal;
    private long passiveEarned;
    private long clickEarned;
    private long passiveRate;
    private long clickCount;
    private double clickRate;
    private long perClickEarnRate;
    private long totalClickEarnRate;

    public ClickMetrics(long startTime, long startTotal) {
        if (startTime == 0L) {
            throw new IllegalArgumentException();
        }
        this.startTime = startTime;
        this.startTotal = startTotal;
    }

    public void endInterval(long endTime, long endTotal, long passiveRate, long clickCount) {
        if (endTime == 0L || endTotal == 0L || clickCount == 0L) {
            throw new IllegalArgumentException();
        }
        this.endTime = endTime;
        this.endTotal = endTotal;
        this.passiveRate = passiveRate;
        this.clickCount = clickCount;

        this.timeDelta = (this.endTime - this.startTime)/1000;
        this.clickRate = this.clickCount/this.timeDelta;
        this.passiveEarned = (long) (this.passiveRate * this.timeDelta);
        this.clickEarned = this.endTotal - this.startTotal - this.passiveEarned;
        this.perClickEarnRate = (long) (this.clickEarned/this.clickCount);
        this.totalClickEarnRate = (long) (this.perClickEarnRate * this.clickRate);
    }

    public void reportMetrics() throws IncompleteMetricsException, Exception {
        if (this.timeDelta == 0L) {
            throw new IncompleteMetricsException();
        }
        SlackReporter.sendSimpleMessage(String.format("Current click rate: %.2f -- Passive earn rate: %s per s -- Click rate: %.2f per s -- Estimated per-click earn rate: %s -- Estimated total click earn rate: %s per s", this.clickRate, NumberUtils.longPrint(this.passiveRate), this.clickRate, NumberUtils.longPrint(this.perClickEarnRate), NumberUtils.longPrint(this.totalClickEarnRate)));
    }

    public void reportPrediction(long remainingClicks) throws Exception {
        double remainingTime = remainingClicks/this.clickRate;
        long prediction = (long) (remainingTime * (this.totalClickEarnRate + this.passiveRate)) + this.endTotal;
        SlackReporter.sendSimpleMessage(String.format("Estimated total at end of process: %s -- Estimated process time remaining: %.2f s", NumberUtils.longPrint(prediction), remainingTime));
    }
}
