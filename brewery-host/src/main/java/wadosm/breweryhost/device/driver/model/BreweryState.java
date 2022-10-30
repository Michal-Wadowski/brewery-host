package wadosm.breweryhost.device.driver.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BreweryState {

    private Boolean power;
    private Boolean motor1;
    private Boolean motor2;
    private Boolean motor3;
    private Integer mains1;
    private Integer mains2;

    public Boolean getMotor(Integer motorNumber) {
        switch (motorNumber) {
            case 1:
                return getMotor1();
            case 2:
                return getMotor2();
            case 3:
                return getMotor3();
            default:
                return null;
        }
    }

    public Integer getMains(Integer mainsNumber) {
        switch (mainsNumber) {
            case 1:
                return getMains1();
            case 2:
                return getMains1();
            default:
                return null;
        }
    }
}
