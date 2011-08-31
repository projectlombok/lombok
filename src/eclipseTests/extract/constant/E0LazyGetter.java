package E0;
import lombok.*;


public class E0LazyGetter {
	/*1: ExtractConstant(5, CONSTANT) :1*/
	@Getter(lazy=true)
	private final int oldName = 1;
	/*:1:*/
}
