import lombok.*;
class DoNothingDueToTopLevel {
  DoNothingDueToTopLevel() {
    super();
  }
  void test() {
    val x = null;
  }
}
class val {
  val() {
    super();
  }
}