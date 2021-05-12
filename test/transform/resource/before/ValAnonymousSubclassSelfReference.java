// issue 2420: to trigger the problem 2 var/val, one normal variable and a anonymous self reference is required
import lombok.val;

public class ValAnonymousSubclassSelfReference {
	public void test() {
		int i = 0;
		val j = 1;
		val k = 2;

		new ValAnonymousSubclassSelfReference() { };
	}
}