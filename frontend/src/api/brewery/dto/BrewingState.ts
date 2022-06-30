import {TemperatureSensor} from '../../configuration/dto/TemperatureSensor'

export class BrewingState {
    enabled: boolean;
    currentTemperature: Array<TemperatureSensor>;
    destinationTemperature: number;
    maxPower: number;
    powerTemperatureCorrelation: number;
    motorEnabled: boolean;
    temperatureAlarm: boolean;
    heatingPower: number;
}
