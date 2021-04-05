import lombok.ToStringFormatter;
import lombok.ToString;

public @ToString class ToStringFormatterStyle {
	
	private String name;
	@ToString.Format(formatter = PasswordFormat.class) private String password;
	private int age;
	@ToString.Format(formatter = CardNumberForamt.class) private long cardNumber;
	
	public long getCardNumber() {
		return this.cardNumber;
	}
	
	class CardNumberForamt implements ToStringFormatter {
		
		@Override public <T> String format(T field) {
			return "formatted : " + field;
		}
	}
	
	class PasswordFormat implements ToStringFormatter {
		
		@Override public <T> String format(T field) {
			return "formatted : " + field;
		}
	}
}