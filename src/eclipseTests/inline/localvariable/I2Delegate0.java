package I2;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Delegate;

public class I2Delegate0 {

	@Delegate(types=SimpleCollection.class)
	private final Collection<String> collection = new ArrayList<String>();
	
	private interface SimpleCollection {
		boolean add(String item);
		boolean remove(Object item);
	}

	public void someMethod(){
		/*1: InlineLocalVariable(localCollection) :1*/
		Collection<String> localCollection = collection;
		/*:1:*/
		System.out.println(localCollection);
	}
}



