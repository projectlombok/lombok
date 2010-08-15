public class Buux extends java.util.ArrayList {
	public Buux() {
		super(7);
		addSomething();
	}
	
	public void addSomething() {
		super.add("H\u3404l\0");
	}
}