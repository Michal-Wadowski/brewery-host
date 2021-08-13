package wadosm.breweryhost.externalinterface.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public abstract class ResponseDTO {

    private Integer commandId;

    private Long time;

}
