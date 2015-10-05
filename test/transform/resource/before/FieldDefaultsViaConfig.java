//CONF: lombok.fieldDefaults.defaultFinal = true
//CONF: lombok.fieldDefaults.defaultPrivate = true
class FieldDefaultsViaConfig1 {
	int x;
	@lombok.experimental.NonFinal int y;
	
	FieldDefaultsViaConfig1(int x) {
		this.x = x;
	}
}

@lombok.experimental.FieldDefaults(level = lombok.AccessLevel.PROTECTED)
class FieldDefaultsViaConfig2 {
	@lombok.experimental.PackagePrivate int x = 2;
	int y = 0;
}
