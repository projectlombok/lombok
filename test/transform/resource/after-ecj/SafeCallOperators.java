import lombok.experimental.SafeCall;
class SafeCallOperators {
  public SafeCallOperators() {
    super();
    @SafeCall Boolean negation;
    {
      java.lang.Boolean negation2 = Boolean();
      boolean negation3 = ((negation2 != null) ? negation2 : false);
      boolean negation1 = (! negation3);
      negation = negation1;
    }
    Integer nullInt = null;
    @SafeCall int minus;
    {
      java.lang.Integer minus2 = nullInt;
      int minus3 = ((minus2 != null) ? minus2 : 0);
      int minus1 = (- minus3);
      minus = minus1;
    }
    @SafeCall int plusAsArgument;
    {
      java.lang.Integer plusAsArgument3 = nullInt;
      int plusAsArgument4 = ((plusAsArgument3 != null) ? plusAsArgument3 : 0);
      int plusAsArgument2 = (+ plusAsArgument4);
      java.lang.Integer plusAsArgument1 = Integer(plusAsArgument2);
      plusAsArgument = ((plusAsArgument1 != null) ? plusAsArgument1 : 0);
    }
    @SafeCall int increase;
    {
      java.lang.Integer increase2 = nullInt;
      int increase3 = ((increase2 != null) ? increase2 : 0);
      int increase1 = (increase3 ++);
      increase = increase1;
    }
    @SafeCall int increaseAsParameter;
    {
      java.lang.Integer increaseAsParameter3 = nullInt;
      int increaseAsParameter4 = ((increaseAsParameter3 != null) ? increaseAsParameter3 : 0);
      int increaseAsParameter2 = (increaseAsParameter4 ++);
      java.lang.Integer increaseAsParameter1 = Integer(increaseAsParameter2);
      increaseAsParameter = ((increaseAsParameter1 != null) ? increaseAsParameter1 : 0);
    }
    @SafeCall int addition = (nullInt + nullInt);
    {
    }
    Integer assignResult = null;
    @SafeCall int assignment = (assignResult = nullInt);
    {
    }
    @SafeCall int compoundAssignment = (assignResult += nullInt);
    {
    }
    @SafeCall int condition = ((nullInt == 0) ? (nullInt + 1) : nullInt);
    {
    }
    @SafeCall byte bitwiseOperators = (byte) (1 | ((Byte() & Byte()) ^ (- 1)));
    {
    }
  }
  public String getNullString() {
    return null;
  }
  public Integer Integer() {
    return null;
  }
  public Byte Byte() {
    return null;
  }
  public Integer Integer(Integer arg) {
    return null;
  }
  public Boolean Boolean() {
    return null;
  }
  public SafeCallOperators nullObject() {
    return null;
  }
}