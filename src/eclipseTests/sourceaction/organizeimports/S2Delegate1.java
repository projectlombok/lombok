package S2;
import nonExistingPackage.*;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Delegate;


public class S2Delegate1 {

	private interface SimpleCollection {
		boolean add(String item);
		/*1: OrganizeImports() :1*/
		boolean remove(Object o);
		/*:1:*/
	}

	@Delegate(types=SimpleCollection.class)
	private final Collection<String> collection = new ArrayList<String>();

	
	
}
