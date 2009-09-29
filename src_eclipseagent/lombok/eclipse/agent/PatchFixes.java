package lombok.eclipse.agent;

public class PatchFixes {
	public static int fixRetrieveStartingCatchPosition(int in) {
		return in;
	}
	
	private static final int BIT24 = 0x800000;
	
	public static boolean checkBit24(Object node) throws Exception {
		int bits = (Integer)(node.getClass().getField("bits").get(node));
		return (bits & BIT24) != 0;
	}
}
