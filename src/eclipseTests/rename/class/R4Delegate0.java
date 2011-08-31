package R4;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Delegate;

public class R4Delegate0 {


	private interface SimpleCollection {
		boolean add(String item);
		boolean remove(Object item);
	}

	@Delegate(types=SimpleCollection.class)
	/*1: RenameClass(R4Delegate0) :1*/
	private final Collection<String> collection = new ArrayList<String>();
	/*:1:*/
}



