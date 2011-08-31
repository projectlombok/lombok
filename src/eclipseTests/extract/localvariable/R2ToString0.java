package R2;
import lombok.ToString;

@ToString
public class R2ToString0 {
	public int oldName = 0;
	
	public static void main(String[] args) {
		
		R2ToString0 object = new R2ToString0();
		
		System.out.println(object.toString());
	}
	
	public void someMethod(){
		int oldName;
		/*1: ExtractLocalVariable(newName) :1*/
		oldName = this.oldName;
		/*:1:*/
	}

}
