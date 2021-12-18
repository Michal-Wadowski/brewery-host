package wadosm.breweryhost.logic;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class DeviceResponse {
    private final String function;
    private final List<Object> arguments;
    private final Object response;
}
