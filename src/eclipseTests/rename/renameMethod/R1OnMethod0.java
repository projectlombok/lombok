package R1;

import lombok.Getter;

public class R1OnMethod0 {

	@Getter(onMethod=@Deprecated) int name = 1;
	
	public int getName(){
		return name;
	}
	
}
