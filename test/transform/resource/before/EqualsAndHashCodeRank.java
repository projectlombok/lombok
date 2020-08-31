import lombok.EqualsAndHashCode;
@EqualsAndHashCode
public class EqualsAndHashCodeRank {
	@EqualsAndHashCode.Include int a;
	@EqualsAndHashCode.Include(rank = 10) int b;
	@EqualsAndHashCode.Include int c;
}
