import lombok.Data;

class DataWithOverrideEqualsAndHashCode {
	class Data1 {
		
	}
	
	@Data
	class Data2 extends Data1 {
		public int hashCode() {
			return 42;
		}
		
		public boolean equals(Object other) {
			return false;
		}
	}
}