package E0;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import lombok.Delegate;

public class E0Delegate0 {


	private interface SimpleCollection {
		boolean add(String item);
		boolean remove(Object item);
	}

	
	/*1: ExtractConstant(5, CONSTANT) :1*/
	@Delegate(types=SimpleCollection.class)
	private final Collection<Integer> collection = new HashSet<Integer>(5);
	/*:1:*/
}



