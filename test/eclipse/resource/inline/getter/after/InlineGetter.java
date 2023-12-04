package pkg;

import lombok.Getter;

@Getter
public class InlineGetter {
	private String string;
	
	public String asReturn() {
		return this.string;
	}
	
	public void asAssignment() {
		String a = this.string;
	}
}