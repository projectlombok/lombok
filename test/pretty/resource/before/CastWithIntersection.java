// version 8:
import java.util.*;
public class CastWithIntersection {
	public void test(List<?> list) {
		RandomAccess r = (RandomAccess & java.io.Serializable) list;
	}
}