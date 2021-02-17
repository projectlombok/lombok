//issue #2752: this test triggers an indirect static access and a non static access warning for the same method call
//platform ecj,eclipse
class ExtensionMethodNonStaticAccess {
  public void method(){
      Derived derived = new Derived();
      derived.staticMethod();
  }
}

class Base {
    static String staticMethod() {
        return "";
    }
}

class Derived extends Base {
    
}