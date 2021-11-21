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

    private Command command;

    private Boolean enable;

    private Integer number;

    private Integer intValue;

    private Float floatValue;

    @Getter
    @ToString
    public enum Command {
        @JsonProperty("Power.powerOff")
        Power_powerOff,
        @JsonProperty("Power.restart")
        Power_restart,

        @JsonProperty("Fermenting.getFermentingState")
        Fermenting_getFermentingState,
        @JsonProperty("Fermenting.setDestinationTemperature")
        Fermenting_setDestinationTemperature,
        @JsonProperty("Fermenting.enable")
        Fermenting_enable,

        @JsonProperty("Brewing.getBrewingState")
        Brewing_getBrewingState,
        @JsonProperty("Brewing.setDestinationTemperature")
        Brewing_setDestinationTemperature,
        @JsonProperty("Brewing.setMaxPower")
        Brewing_setMaxPower,
        @JsonProperty("Brewing.setPowerTemperatureCorrelation")
        Brewing_setPowerTemperatureCorrelation,
        @JsonProperty("Brewing.enableTemperatureAlarm")
        Brewing_enableTemperatureAlarm,
        @JsonProperty("Brewing.enable")
        Brewing_enable,
        @JsonProperty("Brewing.setTimer")
        Brewing_setTimer,
        @JsonProperty("Brewing.removeTimer")
        Brewing_removeTimer,
        @JsonProperty("Brewing.motorEnable")
        Brewing_motorEnable
    }

}
