package jackson;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@lombok.Data
@lombok.AllArgsConstructor
class TestDto1 {

    @com.fasterxml.jackson.annotation.JsonProperty("aListJ")
    private List<Integer> aList;

    @com.fasterxml.jackson.annotation.JsonProperty("bIntegerJ")
    private Integer bInteger;

}

@lombok.Data
@lombok.AllArgsConstructor
class TestDto2 {

    @com.fasterxml.jackson.annotation.JsonProperty("aListJ")
    private final List<Integer> aList;

    @com.fasterxml.jackson.annotation.JsonProperty("bIntegerJ")
    private final Integer bInteger;

}

public class CopyJsonPropertyAnnotationToGetterTest {

    @Test
    public void test() throws Throwable {
        String jsonString1 = new ObjectMapper().writeValueAsString(
                new TestDto1(
                        Arrays.asList(1, 2, 3),
                        10
                )
        );
        System.out.println(jsonString1);

        String jsonString2 = new ObjectMapper().writeValueAsString(
                new TestDto2(
                        Arrays.asList(1, 2, 3),
                        10
                )
        );
        System.out.println(jsonString2);

        assertEquals(jsonString1, jsonString2);
    }

}
