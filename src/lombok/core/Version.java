package lombok.core;

public class Version {
	private static final String VERSION = "0.3.0";
	
	private Version() {
		//Prevent instantiation
	}
	
	public static void main(String[] args) {
		System.out.println(VERSION);
	}
	
	public static String getVersion() {
		return VERSION;
	}
}
