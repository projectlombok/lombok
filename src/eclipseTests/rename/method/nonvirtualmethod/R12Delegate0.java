package R12;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Delegate;

public class R12Delegate0 {


	private interface SimpleCollection {
		/*1: RenameNonVirtualMethod(newName) :1*/
		boolean add(String item);
		/*:1:*/
		boolean remove(Object item);
	}

	@Delegate(types=SimpleCollection.class)
	private final Collection<String> collection = new ArrayList<String>();

}



