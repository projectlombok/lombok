package R1;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Delegate;


public class R1Delegate1 {

	private interface SimpleCollection {
		boolean add(String item);
		/*1: RenameMethod(contains) :1*/
		boolean remove(Object o);
		/*:1:*/
	}

	@Delegate(types=SimpleCollection.class)
	private final Collection<String> collection = new ArrayList<String>();

	
	
}
