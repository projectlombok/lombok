import lombok.experimental.SafeCall;
class SafeCallArray {
  int[] empty = {};
  public SafeCallArray() {
    super();
    @SafeCall int i4;
    {
      SafeCallArray i43 = nullSafeCallArray();
      java.lang.Integer i42 = ((i43 != null) ? i43.nullIndex() : null);
      int i44 = ((i42 != null) ? i42 : 0);
      int[] i45 = empty;
      int i41 = (((i45 != null) && ((i44 >= 0) && (i44 < i45.length))) ? i45[i44] : 0);
      i4 = i41;
    }
    @SafeCall int i;
    {
      int[] i2 = intNullArray();
      int i1 = (((i2 != null) && (0 < i2.length)) ? i2[0] : 0);
      i = i1;
    }
    @SafeCall int i2;
    {
      int[] i22 = intEmptyArray();
      int i21 = (((i22 != null) && ((1 >= 0) && (1 < i22.length))) ? i22[1] : 0);
      i2 = i21;
    }
    @SafeCall int i3;
    {
      int[] i32 = empty;
      int i31 = (((i32 != null) && (0 < i32.length)) ? i32[0] : 0);
      i3 = i31;
    }
    @SafeCall Integer i5;
    {
      java.lang.Integer[] i52 = IntegerNullArray();
      java.lang.Integer i51 = (((i52 != null) && (0 < i52.length)) ? i52[0] : null);
      i5 = i51;
    }
    @SafeCall int i6;
    {
      int[] i62 = new int[]{};
      int i61 = (((i62 != null) && (0 < i62.length)) ? i62[0] : 0);
      i6 = i61;
    }
  }
  public int[] intNullArray() {
    return null;
  }
  public Integer[] IntegerNullArray() {
    return null;
  }
  public int[] intEmptyArray() {
    return empty;
  }
  public Integer nullIndex() {
    return null;
  }
  public SafeCallArray nullSafeCallArray() {
    return null;
  }
}