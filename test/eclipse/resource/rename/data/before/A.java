package pkg;

import lombok.Data;

@Data
public class A {
	private String string;
	
	public String test() {
		return getString();
	}
}