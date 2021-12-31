package wadosm.breweryhost.device.externalinterface.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
// TODO: Replace generic CommandDTO with specified one per command
@AllArgsConstructor
@NoArgsConstructor
public class CommandDTO {

    private Integer commandId;

    private Boolean enable;

    private Integer number;

    private Integer intValue;

    private Float floatValue;
}
