import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Builder(access = AccessLevel.PRIVATE)
public final class BuilderAccessWithGetter {

    @Getter
    private final String string;
}
