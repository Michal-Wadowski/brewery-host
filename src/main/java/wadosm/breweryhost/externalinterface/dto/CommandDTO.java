package wadosm.breweryhost.externalinterface.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@Data
public class CommandDTO {

    private Integer commandId;

    private Command command;

    private Boolean enable;

    private Integer number;

    private Integer intValue;

    private Float floatValue;

    @Getter
    @ToString
    @AllArgsConstructor
    public enum Command {
        @JsonProperty("Power.powerOff")
        Power_powerOff,
        @JsonProperty("Power.restart")
        Power_restart,

        @JsonProperty("Fermenting.getFermentingStatus")
        Fermenting_getFermentingStatus,
        @JsonProperty("Fermenting.setDestinationTemperature")
        Fermenting_setDestinationTemperature,
        @JsonProperty("Fermenting.enable")
        Fermenting_enable,

        @JsonProperty("Brewing.getBrewingStatus")
        Brewing_getBrewingStatus,
        @JsonProperty("Brewing.setDestinationTemperature")
        Brewing_setDestinationTemperature,

        @JsonProperty("Brewing.setMaxPower")
        Brewing_setMaxPower,

        @JsonProperty("Brewing.setPowerTemperatureCorrelation")
        Brewing_setPowerTemperatureCorrelation,

        @JsonProperty("Brewing.enable")
        Brewing_enable
    }

}
