public final class ValueStaticConstructorOf {
	private final String name;
	private final Double price;
	private ValueStaticConstructorOf(String name, Double price) {
		this.name = name;
		this.price = price;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static ValueStaticConstructorOf of(final String name, final Double price) {
		return new ValueStaticConstructorOf(name, price);
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public String getName() {
		return this.name;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public Double getPrice() {
		return this.price;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof ValueStaticConstructorOf)) return false;
		final ValueStaticConstructorOf other = (ValueStaticConstructorOf) o;
		final java.lang.Object this$price = this.getPrice();
		final java.lang.Object other$price = other.getPrice();
		if (this$price == null ? other$price != null : !this$price.equals(other$price)) return false;
		final java.lang.Object this$name = this.getName();
		final java.lang.Object other$name = other.getName();
		if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
		return true;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final java.lang.Object $price = this.getPrice();
		result = result * PRIME + ($price == null ? 43 : $price.hashCode());
		final java.lang.Object $name = this.getName();
		result = result * PRIME + ($name == null ? 43 : $name.hashCode());
		return result;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public java.lang.String toString() {
		return "ValueStaticConstructorOf(name=" + this.getName() + ", price=" + this.getPrice() + ")";
	}
}
