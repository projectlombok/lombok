package S1;
import nonExistingPackage.*;

import lombok.Synchronized;

public class S1Synchronized0 {
	/*1: ToggleComment() :1*/
	@Synchronized
	private void go() {
		throw new Throwable();
	}
	/*:1:*/
}
