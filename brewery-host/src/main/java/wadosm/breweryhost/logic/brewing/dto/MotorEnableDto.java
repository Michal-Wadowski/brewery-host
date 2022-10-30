package wadosm.breweryhost.logic.brewing.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class MotorEnableDto {
    @NotNull
    private Boolean enable;
}
