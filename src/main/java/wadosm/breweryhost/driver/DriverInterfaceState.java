package wadosm.breweryhost.driver;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DriverInterfaceState {

    private Boolean power;
    private Boolean motor1;
    private Boolean motor2;
    private Boolean motor3;
    private Integer sound;
    private Integer mains1;
    private Integer mains2;

}
