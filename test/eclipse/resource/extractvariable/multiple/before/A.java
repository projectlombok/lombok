package pkg;

import java.util.Arrays;

import lombok.Getter;

@Getter
public class A {
	private String string;
	
	public List<String> test() {
		return Arrays.asList(getString(), getString(), getString());
	}
}