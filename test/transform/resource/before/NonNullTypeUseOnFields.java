//version 8:
import lombok.NonNull;

@lombok.RequiredArgsConstructor
@lombok.AllArgsConstructor
@lombok.Setter
@lombok.With
class NonNullTypeUseOnFields {
	@NonNull String[] elementType;
	String @NonNull [] array;
	String @NonNull [][] outerArray;
	// Here @NonNull marks the inner String[] dimension, not the outer field reference.
	String[] @NonNull [] innerArray;
	java.lang.@NonNull String[] elements;
	java.util.List<@NonNull String> listElements;
	java.util.Map<@NonNull String, java.util.List<Object @NonNull []>> nested;
	java.util.@NonNull List<String> list;
}
