package pkg;

import java.util.Arrays;

import lombok.Getter;

@Getter
public class A {
	private String string;
	
	public List<String> test() {
		String temp = getString();
		return Arrays.asList(temp, temp, temp);
	}
}