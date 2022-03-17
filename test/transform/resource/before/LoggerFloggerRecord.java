// version 14:

import lombok.extern.flogger.Flogger;

class LoggerFloggerRecord {
	@Flogger
	record Inner(String x) {}
}
