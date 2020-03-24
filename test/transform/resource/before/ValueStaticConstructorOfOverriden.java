import lombok.Value;
@Value(staticConstructor = "of")
public class ValueStaticConstructorOfOverriden {
	String name;
	Double price;
	
	private ValueStaticConstructorOfOverriden(String name, Double price) {
		this.name = name;
		this.price = price;
	}
	
	public static ValueStaticConstructorOfOverriden of(String name, Double price) {
		return new ValueStaticConstructorOfOverriden(name,price);		
	}
}
