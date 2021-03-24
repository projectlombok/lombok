// version 14:

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
public record ConstructorsOnRecord(String a, String b) {
}