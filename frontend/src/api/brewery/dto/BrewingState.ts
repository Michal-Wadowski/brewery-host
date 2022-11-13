import {TemperatureSensor} from '../../configuration/dto/TemperatureSensor'
import {BrewingReadings} from './BrewingReadings'
import {BrewingSettings} from './BrewingSettings'

export class BrewingState {
    readings: BrewingReadings;
    settings: BrewingSettings;
}
