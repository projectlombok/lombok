package R1;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Delegate;

public class R1Delegate0 {


	private interface SimpleCollection {
		/*1: RenameMethod(newName) :1*/
		boolean add(String item);
		/*:1:*/
		boolean remove(Object item);
	}

	@Delegate(types=SimpleCollection.class)
	private final Collection<String> collection = new ArrayList<String>();

}



