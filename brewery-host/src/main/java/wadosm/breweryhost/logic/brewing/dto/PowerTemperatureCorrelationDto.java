package wadosm.breweryhost.logic.brewing.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@NoArgsConstructor
public class PowerTemperatureCorrelationDto {
    @Min(0)
    @Max(100)
    private Float powerTemperatureCorrelation;
}
