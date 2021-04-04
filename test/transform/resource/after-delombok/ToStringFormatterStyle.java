import lombok.ToStringFormatter;

public class ToStringFormatterStyle {

	class PasswordFormat implements ToStringFormatter {
		@Override
		public <T> String format(T field) {
			return "formatted : " + field;
		}
	}

	private String password;


	class CardNumberForamt implements ToStringFormatter {
		@Override
		public <T> String format(T field) {
			return "formatted : " + field;
		}
	}

	private long cardNumber;

	public long getCardNumber() {
		return this.cardNumber;
	}

	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public java.lang.String toString() {
		return "ToStringFormatterStyle(password=" + new PasswordFormat().format(this.password) + ", cardNumber=" + new CardNumberForamt().format(this.getCardNumber()) + ")";
	}
}