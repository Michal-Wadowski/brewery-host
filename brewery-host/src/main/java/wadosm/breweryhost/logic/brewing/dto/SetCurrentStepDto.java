package wadosm.breweryhost.logic.brewing.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@NoArgsConstructor
public class SetCurrentStepDto {
    @Min(0)
    private Integer currentStep;
}
