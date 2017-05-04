import lombok.experimental.SafeCall;
class SafeCallArray {
  int[] empty = {};
  public SafeCallArray() {
    super();
    @SafeCall int i4;
    {
      SafeCallArray i44 = nullSafeCallArray();
      java.lang.Integer i43 = ((i44 != null) ? i44.nullIndex() : null);
      int i42 = (- i43);
      int[] i45 = empty;
      int i41 = (((i45 != null) && ((i42 >= 0) && (i42 < i45.length))) ? i45[i42] : 0);
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
      int i62 = (- 1);
      int[] i65 = empty;
      int i64 = (((i65 != null) && (0 < i65.length)) ? i65[0] : 0);
      java.lang.Integer i66 = nullIndex();
      int i67 = ((i66 != null) ? i66 : 0);
      int[] i63 = new int[]{i64, i67, 3};
      int i61 = (((i63 != null) && ((i62 >= 0) && (i62 < i63.length))) ? i63[i62] : 0);
      i6 = i61;
    }
    @SafeCall int[] iAr;
    {
      int[] iAr3 = empty;
      int iAr2 = (((iAr3 != null) && (0 < iAr3.length)) ? iAr3[0] : 0);
      java.lang.Integer[] iAr5 = IntegerNullArray();
      java.lang.Integer iAr4 = (((iAr5 != null) && (0 < iAr5.length)) ? iAr5[0] : null);
      int iAr6 = ((iAr4 != null) ? iAr4 : 0);
      java.lang.Integer iAr7 = nullIndex();
      int iAr8 = ((iAr7 != null) ? iAr7 : 0);
      java.lang.Integer iAr9 = (Integer) null;
      int iAr10 = ((iAr9 != null) ? iAr9 : 0);
      int[] iAr1 = new int[]{iAr2, iAr6, iAr8, iAr10, 1};
      iAr = iAr1;
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