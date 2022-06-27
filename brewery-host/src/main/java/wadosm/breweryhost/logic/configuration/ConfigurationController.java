package wadosm.breweryhost.logic.configuration;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;
import wadosm.breweryhost.device.temperature.TemperatureProvider;
import wadosm.breweryhost.device.temperature.TemperatureSensor;
import wadosm.breweryhost.logic.brewing.BrewingService;
import wadosm.breweryhost.logic.brewing.BrewingState;
import wadosm.breweryhost.logic.brewing.dto.*;

import javax.validation.Valid;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("/configuration")
public class ConfigurationController {

    private final BrewingService brewingService;

    private final TemperatureProvider temperatureProvider;

    public ConfigurationController(BrewingService brewingService, TemperatureProvider temperatureProvider) {
        this.brewingService = brewingService;
        this.temperatureProvider = temperatureProvider;
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

    @GetMapping("/getTemperatureSensors")
    public List<TemperatureSensor> getTemperatureSensors() {
        return temperatureProvider.getTemperatureSensors();
    }
}
