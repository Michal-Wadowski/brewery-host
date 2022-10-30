import {TemperatureSensor} from './dto/TemperatureSensor'
import {SensorsConfiguration} from './dto/SensorsConfiguration'
import {ShowSensorDto} from './dto/ShowSensorDto'
import {UseSensorDto} from './dto/UseSensorDto'

export interface ConfigurationEndpoint {

    getTemperatureSensors(): Promise<Array<TemperatureSensor>>;

    getSensorsConfiguration(): Promise<SensorsConfiguration>;

    showSensor(showSensorDto: ShowSensorDto): Promise<void>;

    useSensor(useSensorDto: UseSensorDto): Promise<void>;

}