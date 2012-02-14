import lombok.val;
public class ValComplex {
  private String field = "";
  private static final int CONSTANT = 20;
  <clinit>() {
  }
  public ValComplex() {
    super();
  }
  public void testComplex() {
    final @val char[] shouldBeCharArray = field.toCharArray();
    final @val int shouldBeInt = CONSTANT;
    final @val java.lang.Object lock = new Object();
    synchronized (lock)
      {
        final @val int field = 20;
        final @val int inner = 10;
        switch (field) {
        case 5 : ;
            final @val char[] shouldBeCharArray2 = shouldBeCharArray;
            final @val int innerInner = inner;
        }
      }
    final @val java.lang.String shouldBeString = field;
  }
}