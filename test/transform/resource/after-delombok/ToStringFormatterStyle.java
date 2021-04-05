import lombok.ToStringFormatter;

public class ToStringFormatterStyle {
	private String name;
	private String password;
	private int age;
	private long cardNumber;

	public long getCardNumber() {
		return this.cardNumber;
	}


	class CardNumberForamt implements ToStringFormatter {
		@Override
		public <T> String format(T field) {
			return "formatted : " + field;
		}
	}


	class PasswordFormat implements ToStringFormatter {
		@Override
		public <T> String format(T field) {
			return "formatted : " + field;
		}
	}

	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public java.lang.String toString() {
		return "ToStringFormatterStyle(name=" + this.name + ", password=" + new PasswordFormat().format(this.password) + ", age=" + this.age + ", cardNumber=" + new CardNumberForamt().format(this.getCardNumber()) + ")";
	}
}