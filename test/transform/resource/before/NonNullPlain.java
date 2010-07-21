class NonNullPlain {
	@lombok.Setter
	@lombok.NonNull
	@lombok.Getter int i;
	
	@lombok.Getter
	@lombok.Setter
	@lombok.NonNull
	String s;
}