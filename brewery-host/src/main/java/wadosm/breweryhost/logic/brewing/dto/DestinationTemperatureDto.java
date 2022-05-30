package wadosm.breweryhost.logic.brewing.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Data
@NoArgsConstructor
public class DestinationTemperatureDto {
    @Min(0)
    @Max(100)
    private Float temperature;
}
