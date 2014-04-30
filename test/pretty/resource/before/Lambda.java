// version 8:
public class Lambda {
	Runnable r = () -> System.out.println();
	java.util.Comparator<Integer> c1 = (a, b) -> a - b;
	java.util.Comparator<Integer> c2 = (Integer a, Integer b) -> {
		return a - b;
	};
}