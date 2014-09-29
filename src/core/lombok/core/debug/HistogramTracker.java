package lombok.core.debug;

import java.io.PrintStream;
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
	private static final long[] RANGES = {
		250001L, 500001L, 1000001L, 2000001L, 4000001L, 8000001L, 16000001L, 32000001L,
		64000001L, 128000001L, 256000001L, 512000001L, 1024000001L, 2048000001L, 10000000001L};
	private static final long REPORT_WINDOW = 1000 * 60;
	
	private final String category;
	private final AtomicStampedReference<long[]> bars = new AtomicStampedReference<long[]>(new long[RANGES.length + 2], 0);
	private final AtomicBoolean addedSysHook = new AtomicBoolean(false);
	private final PrintStream out;
	
	public HistogramTracker(String category) {
		this.category = category;
		this.out = null;
		printInit();
	}
	
	public HistogramTracker(String category, PrintStream out) {
		this.category = category;
		this.out = out;
		printInit();
	}
	
	private void printInit() {
		if (category == null) {
			if (out == null) ProblemReporter.info("Initialized histogram", null);
			else out.println("Initialized histogram");
		} else {
			if (out == null) ProblemReporter.info(String.format("Initialized histogram tracker for '%s'",  category), null);
			else out.printf("Initialized histogram tracker for '%s'%n", category);
		}
	}
	
	public long start() {
		return System.nanoTime();
	}
	
	public void end(long startToken) {
		if (!addedSysHook.getAndSet(true)) Runtime.getRuntime().addShutdownHook(new Thread("Histogram Printer") {
			@Override public void run() {
				int[] currentInterval = {0};
				long[] b = bars.get(currentInterval);
				printReport(currentInterval[0], b);
			}
		});
		
		long end = System.nanoTime();
		long now = System.currentTimeMillis();
		long delta = end - startToken;
		if (delta < 0L) delta = 0L;
		int interval = (int) (now / REPORT_WINDOW);
		int[] currentInterval = {0};
		long[] bars = this.bars.get(currentInterval);
		long[] newBars;
		
		if (currentInterval[0] != interval) {
			printReport(currentInterval[0], bars);
			newBars = new long[RANGES.length + 2];
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
	
	private void printReport(int interval, long[] bars) {
		StringBuilder sb = new StringBuilder();
		if (category != null) sb.append(category).append(" ");
		sb.append("[");
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(interval * REPORT_WINDOW);
		int hour = gc.get(Calendar.HOUR_OF_DAY);
		int minute = gc.get(Calendar.MINUTE);
		if (hour < 10) sb.append('0');
		sb.append(hour).append(":");
		if (minute < 10) sb.append('0');
		sb.append(minute).append("] {");
		
		long sum = bars[RANGES.length];
		int count = 0;
		int lastZeroPos = sb.length();
		for (int i = 0; i < RANGES.length; i++) {
			sum += bars[i];
			sb.append(bars[i]);
			if (bars[i] != 0) lastZeroPos = sb.length();
			sb.append(" ");
			count++;
			if (count == 3) sb.append("-- ");
			if (count == 9) sb.append("-- ");
		}
		
		
		if (sum == 0) return;
		sb.setLength(lastZeroPos);
		
		double millis = bars[RANGES.length + 1] / 1000000.0;
		
		long over = bars[RANGES.length];
		if (over > 0L) {
				sb.append(" -- ").append(bars[RANGES.length]);
		}
		sb.append("} total calls: ").append(sum).append(" total time (millis): ").append((int)(millis + 0.5));
		
		if (out == null) ProblemReporter.info(sb.toString(), null);
		else out.println(sb.toString());
	}
}
