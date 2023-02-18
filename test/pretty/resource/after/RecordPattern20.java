record Point(int x, int y) {
}
record Rectangle(Point upperLeft, Point lowerRight) {
}

public class RecordPattern20 {
	void recordPattern(Object o) {
		if (o instanceof Point(int x, int y)) {
		}
		if (o instanceof Rectangle(Point(var x1, var y1), Point(int x2, int y2))) {
		}
	}

	void forEachSimple(Point[] pointArray) {
		for (Point(var x, var y) : pointArray) {
		}
	}
	void forEachNested(Rectangle[] rectangleArray) {
		for (Rectangle(Point(var x1, var y1), Point p) : rectangleArray) {
		}
	}
}