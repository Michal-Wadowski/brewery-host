package wadosm.breweryhost.logic;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class DeviceResponse {
    private String function;
    private List<Object> arguments;
    private Object response;
}
