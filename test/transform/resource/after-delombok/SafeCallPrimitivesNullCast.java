
class SafeCallPrimitivesNullCast {
	public SafeCallPrimitivesNullCast() {
		byte b;
		{
			java.lang.Byte b1 = (Byte) null;
			b = b1 != null ? b1 : 0;
		}
		int i;
		{
			java.lang.Integer i1 = (Integer) null;
			i = i1 != null ? i1 : 0;
		}
		long l;
		{
			java.lang.Long l1 = (Long) null;
			l = l1 != null ? l1 : 0L;
		}
		short s;
		{
			java.lang.Short s1 = (Short) null;
			s = s1 != null ? s1 : 0;
		}
		float f;
		{
			java.lang.Float f1 = ((Float) null);
			f = f1 != null ? f1 : 0.0F;
		}
		double d;
		{
			java.lang.Double d1 = (Double) (null);
			d = d1 != null ? d1 : 0.0;
		}
		char c;
		{
			java.lang.Character c1 = (((Character) ((null))));
			c = c1 != null ? c1 : '\000';
		}
		boolean bool;
		{
			java.lang.Boolean bool1 = (Boolean) null;
			bool = bool1 != null ? bool1 : false;
		}
	}
}
