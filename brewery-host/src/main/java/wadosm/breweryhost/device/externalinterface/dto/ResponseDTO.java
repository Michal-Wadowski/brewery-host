package wadosm.breweryhost.device.externalinterface.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
// TODO: Add command status (OK/FAIL)
public abstract class ResponseDTO {

    private Integer commandId;

    private Long time;

}
