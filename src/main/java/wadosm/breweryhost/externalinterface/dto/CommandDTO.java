package wadosm.breweryhost.externalinterface.dto;

import lombok.Data;

@Data
public class CommandDTO {
    private Integer commandId;
    private String command;
    private Boolean enable;
    private Integer number;
    private Integer value;

    enum Command {

    }
}
