import lombok.Value;
@Value(staticConstructor = "of")
public class ValueStaticConstructorOf {
	
    String name;
    Double price;
	
    private ValueStaticConstructorOf(String name, Double price) {
    	this.name = name;
    	this.price = price;
    }
}
