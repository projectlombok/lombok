package pkg;

import lombok.Getter;

@Getter
public class A {
	private String string;
	
	public String test() {
		String temp = getString();
		return temp;
	}
}