import lombok.experimental.SafeCall;
import java.util.Arrays;
import java.util.List;
class SafeCallDefaultValue {
  enum SomeEum {
    first(),
    second(),
    third(),
    <clinit>() {
    }
    SomeEum() {
      super();
    }
  }
  {
    final Integer defaultInteger = (int) System.nanoTime();
    final int defaultint = (- 1);
    List<Integer> integers = Arrays.<Integer>asList(1, 2, 3);
    @SafeCall int listElem1;
    {
      java.util.List<java.lang.Integer> listElem12 = integers;
      int listElem11 = ((listElem12 != null) ? listElem12.size() : defaultint);
      listElem1 = listElem11;
    }
    @SafeCall int listElem2;
    {
      java.util.List<java.lang.Integer> listElem22 = integers;
      java.lang.Integer listElem21 = ((listElem22 != null) ? listElem22.get(1) : null);
      listElem2 = ((listElem21 != null) ? listElem21 : (- 1));
    }
    final String defaultString = String.valueOf(defaultInteger);
    List<? extends CharSequence> charSequences = Arrays.<CharSequence>asList("", null, new StringBuilder());
    @SafeCall CharSequence listElem3;
    {
      java.util.List<? extends java.lang.CharSequence> listElem32 = charSequences;
      java.lang.CharSequence listElem31 = ((listElem32 != null) ? listElem32.get(0) : null);
      listElem3 = ((listElem31 != null) ? listElem31 : "");
    }
    @SafeCall CharSequence listElem4;
    {
      java.util.List<? extends java.lang.CharSequence> listElem42 = charSequences;
      java.lang.CharSequence listElem41 = ((listElem42 != null) ? listElem42.get(0) : null);
      listElem4 = ((listElem41 != null) ? listElem41 : defaultString);
    }
    List<SomeEum> enums = Arrays.<SomeEum>asList(SomeEum.first, SomeEum.second);
    @SafeCall SomeEum listElem5;
    {
      java.util.List<SafeCallDefaultValue.SomeEum> listElem52 = enums;
      SafeCallDefaultValue.SomeEum listElem51 = ((listElem52 != null) ? listElem52.get(0) : null);
      listElem5 = ((listElem51 != null) ? listElem51 : SomeEum.third);
    }
  }
  SafeCallDefaultValue() {
    super();
  }
}