import lombok.ToStringFormatter;
import lombok.ToString;
public @ToString class ToStringFormatterStyle {
  class CardNumberForamt implements ToStringFormatter {
    CardNumberForamt() {
      super();
    }
    public @Override <T>String format(T field) {
      return ("formatted : " + field);
    }
  }
  class PasswordFormat implements ToStringFormatter {
    PasswordFormat() {
      super();
    }
    public @Override <T>String format(T field) {
      return ("formatted : " + field);
    }
  }
  private String name;
  private @ToString.Format(formatter = PasswordFormat.class) String password;
  private int age;
  private @ToString.Format(formatter = CardNumberForamt.class) long cardNumber;
  public ToStringFormatterStyle() {
    super();
  }
  public long getCardNumber() {
    return this.cardNumber;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (((((((("ToStringFormatterStyle(name=" + this.name) + ", password=") + new PasswordFormat().format(this.password)) + ", age=") + this.age) + ", cardNumber=") + new CardNumberForamt().format(this.getCardNumber())) + ")");
  }
}