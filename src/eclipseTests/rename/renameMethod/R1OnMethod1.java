package R1;

import lombok.Getter;

public class R1OnMethod1 {

	@Getter int name = 1;
	
	@Deprecated
	public int getName(){
		return name;
	}

	
	
}
