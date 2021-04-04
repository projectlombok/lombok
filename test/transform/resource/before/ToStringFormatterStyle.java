import lombok.ToStringFormatter;
import lombok.ToString;

public @ToString class ToStringFormatterStyle {
	class PasswordFormat implements ToStringFormatter {
		
		@Override public <T> String format(T field) {
			return "formatted : " + field;
		}
	}
	
	@ToString.Format(formatter = PasswordFormat.class) private String password;
	
	class CardNumberForamt implements ToStringFormatter {
		
		@Override public <T> String format(T field) {
			return "formatted : " + field;
		}
	}
	
	@ToString.Format(formatter = CardNumberForamt.class) private long cardNumber;
	
	public long getCardNumber() {
		return this.cardNumber;
	}
}