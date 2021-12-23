package wadosm.breweryhost.logic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DeviceResponse {
    private String function;
    private List<Object> arguments;
    private Object response;
}
