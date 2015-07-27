import java.util.*;
public class Cast {
	public void test(List<?> list) {
		RandomAccess r = (/*before*/ RandomAccess /*after*/) list;
	}
}