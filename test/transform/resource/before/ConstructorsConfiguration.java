//CONF: lombok.anyConstructor.suppressConstructorProperties = true
@lombok.AllArgsConstructor
class ConstructorsConfiguration {
	int x;
}
@lombok.AllArgsConstructor(suppressConstructorProperties=false)
class ConstructorsConfigurationExplicit {
	int x;
}
