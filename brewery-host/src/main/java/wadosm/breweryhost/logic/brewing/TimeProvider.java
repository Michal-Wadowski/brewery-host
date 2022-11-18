package wadosm.breweryhost.logic.brewing;

import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TimeProvider {
    public Instant getCurrentTime() {
        return Instant.now();
    }
}
