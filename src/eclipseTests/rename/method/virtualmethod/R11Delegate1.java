package R11;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Delegate;


public class R11Delegate1 {

	public interface SimpleCollection {
		boolean add(String item);
		/*1: RenameVirtualMethod(contains) :1*/
		boolean remove(Object o);
		/*:1:*/
	}

	@Delegate(types=SimpleCollection.class)
	public final Collection<String> collection = new ArrayList<String>();

	
	
}
