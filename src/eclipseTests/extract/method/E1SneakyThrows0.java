import lombok.*;
public class E1SneakyThrows0 {
	
	static final int target = 0;
	
	@SneakyThrows
	public Integer someMethod(){
		System.out.println("a");
		/* 1:ExtractMethod(anotherMethod):1*/
		System.out.println("b");
		/*:1:*/
		System.out.println("c");
		return (Integer)target;
	}
}