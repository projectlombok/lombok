// version 14:
import lombok.NonNull;
public record NonNullOnRecordExistingSetter(@NonNull String a) {
	public NonNullOnRecordExistingSetter(String a) {
		this.a = a;
	}
	public void method(@NonNull String param) {
		if (param == null) {
			throw new java.lang.NullPointerException("param is marked non-null but is null");
		}
		String asd = "a";
	}
}
