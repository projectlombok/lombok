import java.util.Date;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Onstruct;
import lombok.Onstruct.Cml;
import lombok.Onstruct.SourceType;
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
		Object author, editionDate = mybook;// var author = mybook.getAuthor()
		@Onstruct(source=SourceType.FLUENT)
		Object name = mybook;// var name = mybook.name()
		@Onstruct(source=SourceType.BOOL)
		Object purchasable = mybook;// var purchasable = mybook.isPurchasable()
	}

	void testVarPrefix() {
		Book mybook = new Book("author0", "bookname0", new Date(), true);
		@Onstruct(pre = "b_")
		Object author, editionDate = mybook;
		@Onstruct(pre="b_", methodPre = "is")
		Object purchasable = mybook;
	}

	void testOverWriteSourceType() {
		Book mybook = new Book("author0", "bookname0", new Date(), true);
		@Onstruct(source = SourceType.FLUENT, methodCml = Cml.CML)
		Object name=mybook; // var name = mybook.Name()
	}

}
