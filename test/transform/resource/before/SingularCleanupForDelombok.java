// version 8:
// issue #1377: @Singular is not removed by delombok.

import java.util.Set;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Accessors(fluent = true) @AllArgsConstructor(access = AccessLevel.PACKAGE) @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE) class Book {
	@Getter @NonNull String name;
	@Getter @Singular @NonNull Set<String> authors;
	int numberOfAuthors;
	
	@Builder public Book(String name, @Singular Set<String> authors) {
		this(name, authors, authors.size());
	}
}