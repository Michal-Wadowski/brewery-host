package wadosm.breweryhost.logic.brewing;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;
import wadosm.breweryhost.logic.brewing.dto.*;
import wadosm.breweryhost.logic.brewing.model.BrewingSnapshotState;

import javax.validation.Valid;

@Log4j2
@RestController
@RequestMapping("/brewing")
public class BrewingController {

    private final BrewingService brewingService;

    public BrewingController(BrewingService brewingService) {
        this.brewingService = brewingService;
    }

    @GetMapping("/getBrewingState")
    public BrewingSnapshotState getBrewingState() {
        return brewingService.getBrewingSnapshotState();
    }

    @PostMapping("/enable")
    public void enable(@Valid @RequestBody EnableDto enable) {
        brewingService.enable(enable.getEnable());
    }

    @PostMapping("/enableTemperatureAlarm")
    public void enableTemperatureAlarm(@Valid @RequestBody EnableTemperatureAlarmDto enable) {
        brewingService.enableTemperatureAlarm(enable.getEnable());
    }

    @PostMapping("/motorEnable")
    public void motorEnable(@Valid @RequestBody MotorEnableDto enable) {
        brewingService.motorEnable(enable.getEnable());
    }


    @PostMapping("/setDestinationTemperature")
    public void setDestinationTemperature(@Valid @RequestBody DestinationTemperatureDto temperature) {
        brewingService.setDestinationTemperature(temperature.getTemperature());
    }

    @PostMapping("/setMaxPower")
    public void setMaxPower(@Valid @RequestBody MaxPowerDto maxPower) {
        brewingService.setMaxPower(maxPower.getPower());
    }

    @PostMapping("/setPowerTemperatureCorrelation")
    public void setPowerTemperatureCorrelation(@Valid @RequestBody PowerTemperatureCorrelationDto correlation) {
        brewingService.setPowerTemperatureCorrelation(correlation.getPowerTemperatureCorrelation());
    }
}
