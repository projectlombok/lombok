//CONF: lombok.builder.setterPrefix = with

import java.util.List;

@lombok.Builder(access = lombok.AccessLevel.PROTECTED)
class BuilderSimpleWithSetterPrefix<T> {
	private int unprefixed;
}
