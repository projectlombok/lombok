@lombok.extern.slf4j.Log
interface LoggerSlf4jTypesInterface {
}
@lombok.extern.slf4j.Log
@interface LoggerSlf4jTypesAnnotation {
}
@lombok.extern.slf4j.Log
enum LoggerSlf4jTypesEnum {
}
@lombok.extern.slf4j.Log
enum LoggerSlf4jTypesEnumWithElement {
	FOO;
}
interface LoggerSlf4jTypesInterfaceOuter {
	@lombok.extern.slf4j.Log
	class Inner {
	}
}