class TestOperators {
	int x = 10;
	public void test() {
		x = 12;
		int a = +x;
		boolean d = true;
		boolean e = false;
		boolean b;
		a = -x;
		b = !d;
		a = ~x;
		a = ++x;
		a = --x;
		a = x++;
		a = x--;
		b = d || e;
		b = d && e;
		a = x | a;
		a = x ^ a;
		a = x & a;
		b = a == x;
		b = a != x;
		b = a < x;
		b = a > x;
		b = a <= x;
		b = a >= x;
		a = a << x;
		a = a >> x;
		a = a >>> x;
		a = a + x;
		a = a - x;
		a = a * x;
		a = a / x;
		a = a % x;
		a |= x;
		a ^= x;
		a &= x;
		a <<= x;
		a >>= x;
		a >>>= x;
		a += x;
		a -= x;
		a *= x;
		a /= x;
		a %= x;
		a = a > x ? 1 : 0;
	}
}
