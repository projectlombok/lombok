package before;
@lombok.slf4j.Log
class LoggerSlf4jWithPackage {
}
class LoggerSlf4jWithPackageOuter {
	@lombok.slf4j.Log
	static class Inner {
	}
}