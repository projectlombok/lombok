//version 9:
import java.io.PrintWriter;
public class TryWithResourcesVarRef {
	{
		PrintWriter pw = new PrintWriter(System.out);
		try (pw) {
			pw.println();
		}
	}
}
