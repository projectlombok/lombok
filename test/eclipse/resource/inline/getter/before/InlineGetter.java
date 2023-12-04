package pkg;

import lombok.Getter;

@Getter
public class InlineGetter {
	private String string;
	
	public String asReturn() {
		return getString();
	}
	
	public void asAssignment() {
		String a = getString();
	}
}