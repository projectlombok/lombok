// version :9
//issue 694: In javac, resolving the RHS (which is what val does) can cause an entire class to be resolved, breaking all usage of val inside that class. This tests that we handle that better.
class ValWithLocalClasses1 {
	{
		lombok.val f2 = new ValWithLocalClasses2() {};
	}
}

class ValWithLocalClasses2 {
	{
		lombok.val f3 = 0;
	}
}