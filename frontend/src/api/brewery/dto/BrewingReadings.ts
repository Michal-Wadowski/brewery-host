import {TemperatureSensor} from '../../configuration/dto/TemperatureSensor'

export class BrewingReadings {
    currentTemperature: Array<TemperatureSensor>;
    heatingPower: number;
}
