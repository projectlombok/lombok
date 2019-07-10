//version :9
//skip compare contents
import lombok.var;

public class VarInForOldMulti {
	public void oldForMulti() {
		for (var i = 0, j = "Hey"; i < 100; ++i) {
			System.out.println(i);
		}
	}
}