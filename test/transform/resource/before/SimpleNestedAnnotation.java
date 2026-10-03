//CONF: lombok.copyableAnnotations += test.lombok.SimpleNestedAnnotation$TA
package test.lombok;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SimpleNestedAnnotation {

	@TA private final int someVal;

	@Retention(RetentionPolicy.RUNTIME)
	public @interface TA {}
}
