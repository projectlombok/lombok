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