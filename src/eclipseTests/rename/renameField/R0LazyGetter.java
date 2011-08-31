package R0;
import lombok.*;


public class R0LazyGetter {
	/*1: RenameField(oldName, newfield) :1*/
	@Getter(lazy=true)
	private final int oldName = 1;
	/*:1:*/
}
