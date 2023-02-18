record Point(int x, int y) {
}
record Rectangle(Point upperLeft, Point lowerRight) {
}

public class RecordPattern19 {
	void recordPattern(Object o) {
		if (o instanceof Point(int x, int y)) {
		}
		if (o instanceof Point(int x, int y) p) {
		}
		if (o instanceof Rectangle(Point(var x1, var y1), Point(int x2, int y2))) {
		}
	}
}