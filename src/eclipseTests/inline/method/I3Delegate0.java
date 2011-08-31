package I3;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Delegate;

public class I3Delegate0 {

	@Delegate(types=SimpleCollection.class)
	private final Collection<String> collection = new ArrayList<String>();
	
	private interface SimpleCollection {
		boolean add(String item);
		boolean remove(Object item);
	}

	public void someMethod() {
		/*1: InlineMethod(anotherMethod) :1*/
		Collection<String> oldName = anotherMethod();
		/*:1:*/
		System.out.println(oldName);
	}
	
	public Collection<String> anotherMethod(){
		Collection<String> localCollection = collection;
		System.out.println(localCollection);
		return localCollection;
	}
}



