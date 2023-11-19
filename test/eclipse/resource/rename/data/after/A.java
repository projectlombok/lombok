package pkg;

import lombok.Data;

@Data
public class A {
	private String newString;
	
	public String test() {
		return getNewString();
	}
}