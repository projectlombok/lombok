package lombok.core.debug;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * Create one of these and call .report() on it a lot to emit histogram data about the times taken by some process.
 * Currently the results are broken down into 10 buckets, from 0 millis to a quarter of a second, and a report is emitted
 * to the ProblemReporter every minute. If there are 0 entries for a given minute, it is not reported. Reports aren't
 * emitted until you call report().
 */
public class HistogramTracker {
	private static final int[] RANGES = {1, 2, 5, 9, 17, 33, 65, 129, 257};
	private static final long REPORT_TIME = 1000 * 60;
	
	private final String category;
	private final AtomicStampedReference<int[]> bars = new AtomicStampedReference<int[]>(new int[RANGES.length + 2], 0);
	private final AtomicBoolean addedSysHook = new AtomicBoolean(false);
	
	public HistogramTracker(String category) {
		this.category = category;
		if (category == null) {
			ProblemReporter.info("Initialized histogram", null);
		} else {
			ProblemReporter.info(String.format("Initialized histogram tracker for '%s'",  category), null);
		}
	}
	
	public void report(long start) {
		if (!addedSysHook.getAndSet(true)) Runtime.getRuntime().addShutdownHook(new Thread("Histogram Printer") {
			@Override public void run() {
				int[] currentInterval = {0};
				int[] b = bars.get(currentInterval);
				printReport(currentInterval[0], b);
			}
		});
		
		long end = System.currentTimeMillis();
		long delta = end - start;
		int interval = (int) (end / REPORT_TIME);
		int[] currentInterval = {0};
		int[] bars = this.bars.get(currentInterval);
		int[] newBars;
		
		if (currentInterval[0] != interval) {
			printReport(currentInterval[0], bars);
			newBars = new int[RANGES.length + 2];
			if (!this.bars.compareAndSet(bars, newBars, currentInterval[0], interval)) {
				newBars = this.bars.get(currentInterval);
			}
		} else {
			newBars = bars;
		}
		
		newBars[RANGES.length + 1] += delta;
		for (int i = 0; i < RANGES.length; i++) {
			if (delta < RANGES[i]) {
				newBars[i]++;
				return;
			}
		}
		
		newBars[RANGES.length]++;
	}
	
	private void printReport(int interval, int[] bars) {
		StringBuilder sb = new StringBuilder();
		if (category != null) sb.append(category).append(" ");
		sb.append("[");
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(interval * REPORT_TIME);
		int hour = gc.get(Calendar.HOUR_OF_DAY);
		int minute = gc.get(Calendar.MINUTE);
		if (hour < 10) sb.append('0');
		sb.append(hour).append(":");
		if (minute < 10) sb.append('0');
		sb.append(minute).append("] {");
		
		int sum = bars[RANGES.length];
		for (int i = 0; i < RANGES.length; i++) {
			sum += bars[i];
			sb.append(bars[i]).append(" ");
		}
		
		if (sum == 0) return;
		
		sb.append(bars[RANGES.length]).append("} total calls: ").append(sum).append(" total time: ").append(bars[RANGES.length + 1]);
		
		ProblemReporter.info(sb.toString(), null);
	}
}
