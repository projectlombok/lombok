package pkg;

import lombok.Builder;
import lombok.Data;
import lombok.With;
import lombok.experimental.WithBy;

@Data
@Builder
@With
@WithBy
public class A {
	private String string;
	
	public void a() {
		this.string = "a";
	}
}