package S1;
import nonExistingPackage.*;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Delegate;

public class S1Delegate0 {


	private interface SimpleCollection {
		/*1: ToggleComment() :1*/
		boolean add(String item);
		/*:1:*/
		boolean remove(Object item);
	}

	@Delegate(types=SimpleCollection.class)
	private final Collection<String> collection = new ArrayList<String>();

}



