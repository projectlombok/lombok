// version 8:
import java.util.Set;
import lombok.NonNull;
class Book {
	@NonNull
	private final String name;
	@NonNull
	private final Set<String> authors;
	private final int numberOfAuthors;
	public Book(String name, Set<String> authors) {
		this(name, authors, authors.size());
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class BookBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String name;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private java.util.ArrayList<String> authors;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		BookBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public Book.BookBuilder name(final String name) {
			this.name = name;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public Book.BookBuilder author(final String author) {
			if (this.authors == null) this.authors = new java.util.ArrayList<String>();
			this.authors.add(author);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public Book.BookBuilder authors(final java.util.Collection<? extends String> authors) {
			if (authors == null) {
				throw new java.lang.NullPointerException("authors cannot be null");
			}
			if (this.authors == null) this.authors = new java.util.ArrayList<String>();
			this.authors.addAll(authors);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public Book.BookBuilder clearAuthors() {
			if (this.authors != null) this.authors.clear();
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public Book build() {
			java.util.Set<String> authors;
			switch (this.authors == null ? 0 : this.authors.size()) {
			case 0: 
				authors = java.util.Collections.emptySet();
				break;
			case 1: 
				authors = java.util.Collections.singleton(this.authors.get(0));
				break;
			default: 
				authors = new java.util.LinkedHashSet<String>(this.authors.size() < 1073741824 ? 1 + this.authors.size() + (this.authors.size() - 3) / 3 : java.lang.Integer.MAX_VALUE);
				authors.addAll(this.authors);
				authors = java.util.Collections.unmodifiableSet(authors);
			}
			return new Book(this.name, authors);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "Book.BookBuilder(name=" + this.name + ", authors=" + this.authors + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static Book.BookBuilder builder() {
		return new Book.BookBuilder();
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	Book(@NonNull final String name, @NonNull final Set<String> authors, final int numberOfAuthors) {
		if (name == null) {
			throw new java.lang.NullPointerException("name is marked non-null but is null");
		}
		if (authors == null) {
			throw new java.lang.NullPointerException("authors is marked non-null but is null");
		}
		this.name = name;
		this.authors = authors;
		this.numberOfAuthors = numberOfAuthors;
	}
	@NonNull
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public String name() {
		return this.name;
	}
	@NonNull
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public Set<String> authors() {
		return this.authors;
	}
}
