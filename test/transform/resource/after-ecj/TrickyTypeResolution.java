import lombok.*;
class TrickyDoNothing {
  @interface Getter {
  }
  @Getter int x;
  TrickyDoNothing() {
    super();
  }
}
class TrickyDoNothing2 {
  @interface Getter {
  }
  @Getter int x;
  TrickyDoNothing2() {
    super();
  }
}
class TrickySuccess {
  @Getter int x;
  public @java.lang.SuppressWarnings("all") int getX() {
    return this.x;
  }
  TrickySuccess() {
    super();
  }
}
class TrickyDoNothing3 {
  TrickyDoNothing3() {
    super();
  }
  void test() {
    class val {
      val() {
        super();
      }
    }
    val x = null;
  }
}
class TrickyDoSomething {
  TrickyDoSomething() {
    super();
  }
  void test() {
    final @val java.lang.Object x = null;
    class val {
      val() {
        super();
      }
    }
  }
}
class DoubleTrickyDoNothing {
  DoubleTrickyDoNothing() {
    super();
  }
  void test() {
    class val {
      val() {
        super();
      }
    }
    for (int i = 10;; (i < 20); i ++) 
      {
        val y = null;
      }
  }
}
class DoubleTrickyDoSomething {
  DoubleTrickyDoSomething() {
    super();
  }
  void test() {
    for (int j = 10;; (j < 20); j ++) 
      {
        class val {
          val() {
            super();
          }
        }
      }
    for (int i = 10;; (i < 20); i ++) 
      {
        final @val java.lang.Object y = null;
      }
  }
}