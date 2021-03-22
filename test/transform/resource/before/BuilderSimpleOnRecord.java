// version 14:

import java.util.List;

@lombok.Builder(access = lombok.AccessLevel.PROTECTED)
record BuilderSimpleOnRecord<T>(List<T> l, String a) {
}
