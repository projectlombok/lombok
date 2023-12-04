package pkg;

import lombok.Setter;

@Setter
public class InlineSetter {
	private String string;
	
	public void test() {
		this.string="test";
	}
}