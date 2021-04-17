package wadosm.breweryhost;

import lombok.Data;

import java.time.Instant;

@Data
public class BreweryStatus {

    private Instant time;
    private Boolean power;
    private Boolean motor1;
    private Boolean motor2;
    private Boolean motor3;
    private Integer sound;
    private Integer mains1;
    private Integer mains2;
    private Integer temperature1;
    private Integer temperature2;

}
