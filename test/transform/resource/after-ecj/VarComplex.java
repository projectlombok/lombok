import lombok.var;
public class VarComplex {
  private String field = "";
  private static final int CONSTANT = 20;
  <clinit>() {
  }
  public VarComplex() {
    super();
  }
  public void testComplex() {
    @var char[] shouldBeCharArray = field.toCharArray();
    @var int shouldBeInt = CONSTANT;
    @var java.lang.Object lock = new Object();
    synchronized (lock)
      {
        @var int field = 20;
        @var int inner = 10;
        switch (field) {
        case 5 :
            @var char[] shouldBeCharArray2 = shouldBeCharArray;
            @var int innerInner = inner;
        }
      }
    @var java.lang.String shouldBeString = field;
  }
}