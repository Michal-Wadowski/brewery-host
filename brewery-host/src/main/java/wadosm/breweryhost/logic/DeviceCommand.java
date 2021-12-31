package wadosm.breweryhost.logic;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class DeviceCommand {
    private final String function;
    private final List<Object> arguments;
}
