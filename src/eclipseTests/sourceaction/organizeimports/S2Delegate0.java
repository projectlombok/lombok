package S2;
import nonExistingPackage.*;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Delegate;

public class S2Delegate0 {


	private interface SimpleCollection {
		/*1: OrganizeImports() :1*/
		boolean add(String item);
		/*:1:*/
		boolean remove(Object item);
	}

	@Delegate(types=SimpleCollection.class)
	private final Collection<String> collection = new ArrayList<String>();

}



