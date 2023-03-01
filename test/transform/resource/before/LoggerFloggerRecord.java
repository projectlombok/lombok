// version 16:

import lombok.extern.flogger.Flogger;

class LoggerFloggerRecord {
	@Flogger
	public record Inner(String x) {}
}
