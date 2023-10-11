import java.util.Date;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Onstruct;
import lombok.core.PrintAST;
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
		@Onstruct
		Object author, editionDate = mybook;
		@Onstruct(methodPre = "")
		Object name = mybook;
		assert Objects.equals(mybook.getAuthor(), author);
		assert Objects.equals(mybook.name(), name);
		assert Objects.equals(mybook.getEditionDate(), editionDate);
		@Onstruct(methodPre = "is")
		Object purchasable = mybook;
		assert Objects.equals(mybook.isPurchasable(), purchasable);
	}

	void testPrefix() {
		Book mybook = new Book("author0", "bookname0", new Date(), true);
		@Onstruct(pre = "b_")
		Object author, editionDate = mybook;
		assert Objects.equals(mybook.getAuthor(), b_author);
		assert Objects.equals(mybook.getEditionDate(), b_editionDate);
		@Onstruct(pre="b_", methodPre = "is")
		Object purchasable = mybook;
		assert Objects.equals(mybook.isPurchasable(), b_purchasable);
	}

}
