@lombok.EqualsAndHashCode(cacheStrategy = lombok.EqualsAndHashCode.CacheStrategy.LAZY)
class EqualsAndHashCode {
	int x;
	boolean[] y;
	Object[] z;
	String a;
	String b;
}

@lombok.EqualsAndHashCode(cacheStrategy = lombok.EqualsAndHashCode.CacheStrategy.LAZY)
final class EqualsAndHashCode2 {
	int x;
	long y;
	float f;
	double d;
	boolean b;
}

@lombok.EqualsAndHashCode(callSuper=false, cacheStrategy = lombok.EqualsAndHashCode.CacheStrategy.LAZY)
final class EqualsAndHashCode3 extends EqualsAndHashCode {
}

@lombok.EqualsAndHashCode(callSuper=true, cacheStrategy = lombok.EqualsAndHashCode.CacheStrategy.LAZY)
class EqualsAndHashCode4 extends EqualsAndHashCode {
}

@lombok.EqualsAndHashCode(callSuper=true, cacheStrategy = lombok.EqualsAndHashCode.CacheStrategy.LAZY)
final class EqualsAndHashCode5 extends EqualsAndHashCode {
}