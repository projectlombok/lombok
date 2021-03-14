//version 8: springframework dep is too new to run on j6
//CONF: lombok.addNullAnnotations = spring
import lombok.With;
class WithPlain {
	@lombok.With int i;
	@With final int foo;
	
	WithPlain(int i, int foo) {
		this.i = i;
		this.foo = foo;
	}
}
