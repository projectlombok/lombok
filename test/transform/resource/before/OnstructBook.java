import java.util.Date;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Onstruct;
import lombok.experimental.Accessors;

public class OnstructBook {

	@lombok.Getter
	@AllArgsConstructor
	public static class Book {

		private String author;
		@Accessors(fluent = true)
		private String name;
		private Date editionDate;
		private boolean purchasable;

	}

	void test() {
		Book mybook = new Book("author0", "bookname0", new Date(), true);
		@Onstruct(prefix = "b_")
		Object author, name, editiondate, purchasable = mybook;
		assert Objects.equals(mybook.getAuthor(), author);
		assert Objects.equals(mybook.name(), name);
		assert Objects.equals(mybook.getEditionDate(), editiondate);
		assert Objects.equals(mybook.isPurchasable(), purchasable);
	}

}
