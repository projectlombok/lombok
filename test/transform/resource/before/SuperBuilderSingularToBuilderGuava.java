public class SuperBuilderSingularToBuilderGuava {
	@lombok.experimental.SuperBuilder(toBuilder=true)
	public static class Parent<T> {
		@lombok.Singular private com.google.common.collect.ImmutableList<T> cards;
		@lombok.Singular private com.google.common.collect.ImmutableCollection<? extends Number> frogs;
		@SuppressWarnings("all") @lombok.Singular("rawSet") private com.google.common.collect.ImmutableSet rawSet;
		@lombok.Singular private com.google.common.collect.ImmutableSortedSet<String> passes;
		@lombok.Singular private com.google.common.collect.ImmutableTable<? extends Number, ? extends Number, String> users;
	}
	
	@lombok.experimental.SuperBuilder(toBuilder=true)
	public static class Child<T> extends Parent<T> {
		private double field3;
	}
	
	public static void test() {
		Child<Integer> x = Child.<Integer>builder().card(1).build().toBuilder().build();
	}
}
