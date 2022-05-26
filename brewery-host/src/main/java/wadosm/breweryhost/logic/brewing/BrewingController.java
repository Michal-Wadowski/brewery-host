package wadosm.breweryhost.logic.brewing;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping("/brewing")
public class BrewingController {

    private final BrewingService brewingService;

    public BrewingController(BrewingService brewingService) {
        this.brewingService = brewingService;
    }

    @GetMapping("/getBrewingState")
    public BrewingState getBrewingState() {
        return brewingService.getBrewingState();
    }

    @PostMapping("/setDestinationTemperature")
    public void setDestinationTemperature(@RequestBody Float temperature) {
        brewingService.setDestinationTemperature(temperature);
    }

    @PostMapping("/enable")
    public void enable(Boolean enable) {
        brewingService.enable(enable);
    }

    @PostMapping("/setMaxPower")
    public void setMaxPower(@RequestBody Integer maxPower) {
        brewingService.setMaxPower(maxPower);
    }

    @PostMapping("/setPowerTemperatureCorrelation")
    public void setPowerTemperatureCorrelation(@RequestBody Float temperatureCorrelation) {
        brewingService.setPowerTemperatureCorrelation(temperatureCorrelation);
    }

    @PostMapping("/enableTemperatureAlarm")
    public void enableTemperatureAlarm(@RequestBody boolean enable) {
        brewingService.enableTemperatureAlarm(enable);
    }

    @PostMapping("/motorEnable")
    public void motorEnable(@RequestBody boolean enable) {
        brewingService.motorEnable(enable);
    }

    @Data
    static class CalibrateTemperatureDto {
        private final Integer side;
        private final Float value;
    }

    @PostMapping("/calibrateTemperature")
    public void calibrateTemperature(@RequestBody CalibrateTemperatureDto calibrateTemperatureDto) {
        brewingService.calibrateTemperature(calibrateTemperatureDto.getSide(), calibrateTemperatureDto.getValue());
    }
}
