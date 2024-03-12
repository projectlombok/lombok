package pkg;

import lombok.experimental.Delegate;

public class DelegateModel {
	
	@Delegate
	private Runnable run;
}