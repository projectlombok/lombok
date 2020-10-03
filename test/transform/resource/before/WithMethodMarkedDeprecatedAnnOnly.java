import lombok.With;

class WithMethodMarkedDeprecatedAnnOnly {
	
	@Deprecated
	@With int annotation;
	
	WithMethodMarkedDeprecatedAnnOnly(int annotation) {
	}
}