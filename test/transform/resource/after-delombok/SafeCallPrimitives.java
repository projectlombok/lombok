
class SafeCallPrimitives {
	public SafeCallPrimitives() {
		int i;
		{
			SafeCallPrimitives i3 = newSafeCallPrimitives();
			java.lang.Integer i2 = i3 != null ? i3.Integer() : null;
			int i1 = i2 != null ? i2.intValue() : 0;
			i = i1;
		}
		int i2;
		{
			java.lang.Integer i21 = Integer();
			i2 = i21 != null ? i21 : 0;
		}
		int i3;
		{
			java.lang.String i33 = getNullString();
			int i32 = i33 != null ? i33.length() : 0;
			int i31 = (int) i32;
			i3 = i31;
		}
		byte bl;
		{
			java.lang.Integer bl3 = Integer();
			long bl2 = bl3 != null ? (long) bl3 : 0L;
			byte bl1 = (byte) bl2;
			bl = bl1;
		}
		long lb;
		{
			java.lang.Integer lb2 = Integer();
			byte lb1 = lb2 != null ? lb2.byteValue() : 0;
			lb = lb1;
		}
		long l;
		{
			java.lang.String l3 = getNullString();
			int l2 = (l3 != null ? l3.length() : 0);
			long l1 = (long) l2;
			l = l1;
		}
		float f;
		{
			java.lang.String f3 = getNullString();
			int f2 = (f3 != null ? f3.length() : 0);
			float f1 = (float) f2;
			f = f1;
		}
		double d;
		{
			java.lang.String d3 = getNullString();
			int d2 = (d3 != null ? d3.length() : 0);
			double d1 = (double) d2;
			d = d1;
		}
		char c;
		{
			java.lang.String c2 = getNullString();
			char c1 = c2 != null ? c2.charAt(0) : '\000';
			c = c1;
		}
		boolean bool;
		{
			java.lang.String bool2 = getNullString();
			boolean bool1 = bool2 != null ? bool2.isEmpty() : false;
			bool = bool1;
		}
	}

	public String getNullString() {
		return null;
	}

	public Integer Integer() {
		return null;
	}

	public SafeCallPrimitives newSafeCallPrimitives() {
		return null;
	}
}