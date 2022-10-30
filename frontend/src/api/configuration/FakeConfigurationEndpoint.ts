import _ from 'lodash'
import {TemperatureSensor} from './dto/TemperatureSensor'
import {SensorsConfiguration} from './dto/SensorsConfiguration'
import {ConfigurationEndpoint} from './ConfigurationEndpoint'
import {ShowSensorDto} from './dto/ShowSensorDto'
import {UseSensorDto} from './dto/UseSensorDto'

export class FakeConfigurationEndpoint implements ConfigurationEndpoint {

    private temperatureSensors: Array<TemperatureSensor> = [
        {sensorId: "foo-123", temperature: 36.6} as TemperatureSensor,
        {sensorId: "bar-987", temperature: 70.3} as TemperatureSensor
    ];

    private sensorsConfiguration = {
        useBrewingSensorIds: ["foo-123"],
        showBrewingSensorIds: ["foo-123", "bar-987"]
    } as SensorsConfiguration;

    getTemperatureSensors(): Promise<Array<TemperatureSensor>> {
        return new Promise<Array<TemperatureSensor>>((resolve, reject) => {
            resolve(this.temperatureSensors);
        });
    }

    getSensorsConfiguration(): Promise<SensorsConfiguration> {
        return new Promise<SensorsConfiguration>((resolve, reject) => {
            resolve(this.sensorsConfiguration);
        });
    }

    showSensor(showSensorDto: ShowSensorDto): Promise<void> {
        return new Promise((resolve) => {
            let showBrewingSensorIds = _.without(
                this.sensorsConfiguration.showBrewingSensorIds,
                showSensorDto.sensorId
            );
            if (showSensorDto.show) {
                showBrewingSensorIds = _.concat(showBrewingSensorIds, showSensorDto.sensorId);
            }
            this.sensorsConfiguration.showBrewingSensorIds = showBrewingSensorIds;
            resolve();
        });
    }

    useSensor(useSensorDto: UseSensorDto): Promise<void> {
        return new Promise((resolve) => {
            let useBrewingSensorIds = _.without(
                this.sensorsConfiguration.useBrewingSensorIds,
                useSensorDto.sensorId
            );
            if (useSensorDto.use) {
                useBrewingSensorIds = _.concat(useBrewingSensorIds, useSensorDto.sensorId);
            }
            this.sensorsConfiguration.useBrewingSensorIds = useBrewingSensorIds;
            resolve();
        });
    }

    getManualConfig(): Promise<string> {
        return new Promise((resolve) => {
            resolve('{"a":1,"b":2,"c":{"d":1,"e":[1,2]}}');
        });
    }
}