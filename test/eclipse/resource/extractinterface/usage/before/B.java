package pkg;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class B {
	private A a = new A();
	private String string = a.getString();
	private int integer = a.getInteger();
}