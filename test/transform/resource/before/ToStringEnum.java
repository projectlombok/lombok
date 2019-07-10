import lombok.ToString;
@ToString
enum ToStringEnum1 {
	CONSTANT;
}
@ToString
enum ToStringEnum2 {
	CONSTANT();
	int x;
	String name;
}
class ToStringEnum3 {
	@ToString 
	enum MemberEnum {
		CONSTANT;
	}
}