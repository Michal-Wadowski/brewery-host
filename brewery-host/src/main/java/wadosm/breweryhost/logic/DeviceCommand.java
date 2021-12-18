package wadosm.breweryhost.logic;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class DeviceCommand {
    private final String function;
    private final List<Object> arguments;
}
