// version 14:

import java.util.Collection;
import java.util.List;

import lombok.Builder;
import lombok.Singular;

@Builder
public record BuilderSingularOnRecord<T>(@Singular List<T> children, @Singular Collection<? extends Number> scarves, @SuppressWarnings("all") @Singular("rawList") List rawList) {
}