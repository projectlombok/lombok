import lombok.extern.flogger.Flogger;

@lombok.extern.flogger.Flogger
class LoggerFlogger {
}

@Flogger
class LoggerFloggerWithImport {
}

class LoggerFloggerOuter {
	@lombok.extern.flogger.Flogger
	static class Inner {
		
	}
}

@Flogger
enum LoggerFloggerWithEnum {
	CONSTANT;
}

class LoggerFloggerWithInnerEnum {
	@Flogger
	enum Inner {
		CONSTANT;
	}
}
