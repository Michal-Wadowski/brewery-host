import _ from 'lodash'
import $ from 'jquery'

import {AbstractEndpoint} from '../AbstractEndpoint'
import {TemperatureSensor} from './dto/TemperatureSensor'
import {SensorsConfiguration} from './dto/SensorsConfiguration'
import {ShowSensorDto} from './dto/ShowSensorDto'
import {UseSensorDto} from './dto/UseSensorDto'

export class ConfigurationEndpoint extends AbstractEndpoint {

    getTemperatureSensors(): Promise<Array<TemperatureSensor>> {
        return this.getRequest<Array<TemperatureSensor>>("/configuration/getTemperatureSensors");
    }

    getSensorsConfiguration(): Promise<SensorsConfiguration> {
        return this.getRequest<SensorsConfiguration>("/configuration/getSensorsConfiguration");
    }

    showSensor(showSensorDto: ShowSensorDto): Promise<void> {
        return this.postRequest<void>("/configuration/showSensor", showSensorDto);
    }

    useSensor(useSensorDto: UseSensorDto): Promise<void> {
        return this.postRequest<void>("/configuration/useSensor", useSensorDto);
    }

    getManualConfig(): Promise<string> {
        return this.getRequest<string>("/configuration/manualConfig");
    }
}