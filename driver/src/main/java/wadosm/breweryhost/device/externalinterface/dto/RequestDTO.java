package wadosm.breweryhost.device.externalinterface.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
// TODO: Replace generic CommandDTO with specified one per command
@AllArgsConstructor
@NoArgsConstructor
public class RequestDTO {

    private String command;

    private Integer commandId;

    private Boolean enable;

    private Integer number;

    private Integer intValue;

    private Float floatValue;
}
