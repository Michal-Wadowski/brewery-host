package wadosm.breweryhost.logic.brewing;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import wadosm.breweryhost.device.temperature.model.TemperatureSensor;
import wadosm.breweryhost.logic.brewing.model.SensorsConfiguration;
import wadosm.breweryhost.logic.general.ConfigProvider;

@Service
@AllArgsConstructor
class CalibrationProvider {

    private ConfigProvider configProvider;

    public TemperatureSensor correctTemperature(TemperatureSensor uncalibratedSensor) {
        if (uncalibratedSensor != null) {
            Double uncalibratedTemperature = uncalibratedSensor.getTemperature();
            var temperatureCalibration = configProvider.loadConfiguration().getSensorsConfiguration().getCalibrationMeasurements();

            if (temperatureCalibration.containsKey(uncalibratedSensor.getSensorId())) {
                var sensorCalibration = temperatureCalibration.get(uncalibratedSensor.getSensorId());

                if (isCalibrationComplete(sensorCalibration)) {
                    var a = (sensorCalibration.getHighDesired() - sensorCalibration.getLowDesired()) / (sensorCalibration.getHighMeasured() - sensorCalibration.getLowMeasured());
                    var b = -a * sensorCalibration.getLowMeasured() + sensorCalibration.getLowDesired();

                    return uncalibratedSensor.withTemperature(getRounded(a * uncalibratedTemperature + b));
                }
            }
        }

        return uncalibratedSensor;
    }

    private static double getRounded(double calibrated) {
        return Math.round(calibrated * 100) / 100.0;
    }

    private static boolean isCalibrationComplete(SensorsConfiguration.SensorCalibration sensorCalibration) {
        return sensorCalibration.getHighDesired() != null
                && sensorCalibration.getLowDesired() != null
                && sensorCalibration.getHighMeasured() != null
                && sensorCalibration.getLowMeasured() != null;
    }
}
