import lombok.Getter;
import pkgA.ClassA;

public class ClassB {
	@Getter private String world = "world";
	public void test() {
		new ClassA().getHello();
		getWorld();
	}
}
