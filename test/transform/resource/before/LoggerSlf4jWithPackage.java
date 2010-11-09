package before;
@lombok.extern.slf4j.Log
class LoggerSlf4jWithPackage {
}
class LoggerSlf4jWithPackageOuter {
	@lombok.extern.slf4j.Log
	static class Inner {
	}
}