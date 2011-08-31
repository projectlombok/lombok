package R11;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Delegate;

public class R11Delegate0 {


	public interface SimpleCollection {
		/*1: RenameVirtualMethod(newName) :1*/
		boolean add(String item);
		/*:1:*/
		boolean remove(Object item);
	}

	@Delegate(types=SimpleCollection.class)
	public final Collection<String> collection = new ArrayList<String>();

}



