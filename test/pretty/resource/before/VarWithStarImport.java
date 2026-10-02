// version 27:
// issue 4082
import lombok.*;

public class VarWithStarImport {
	public static VarWithStarImport create() {
		var entity = new VarWithStarImport();
		return entity;
	}
}
