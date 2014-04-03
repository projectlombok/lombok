@interface Nullable {
}

@lombok.EqualsAndHashCode(onParam=@_{@Nullable})
class EqualsAndHashCodeWithOnParam {
	int x;
	boolean[] y;
	Object[] z;
	String a;
	String b;
}