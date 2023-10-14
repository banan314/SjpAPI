package sjpapi.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;

import java.util.List;

@Value
public class SjpJSON {
    String name;
    int count;
    List<Boolean> canBeUsed;
    List<String> variant;
    List<String> meaning;

    public static SjpJSON empty() {
        return new SjpJSON(
                "",
                0,
                List.of(),
                List.of(),
                List.of()
        );
    }
}
