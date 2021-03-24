// CONF: lombok.log.fieldIsStatic = false
// version 14:

import lombok.extern.slf4j.Slf4j;

@Slf4j
public record LoggerConfigOnRecord(String a, String b) {
}