package R0;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Delegate;

public class R0Delegate0 {


	private interface SimpleCollection {
		boolean add(String item);
		boolean remove(Object item);
	}

	@Delegate(types=SimpleCollection.class)
	/*1: RenameField(collection, newName) :1*/
	private final Collection<String> collection = new ArrayList<String>();
	/*:1:*/
}



