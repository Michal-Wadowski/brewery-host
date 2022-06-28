import {BrewingState} from './dto/BrewingState'

export interface BreweryEndpoint {

    getBrewingState(): Promise<BrewingState>;

    enable(data: any): Promise<void>;

    enableTemperatureAlarm(data: any): Promise<void>;

    motorEnable(data: any): Promise<void>;

    setDestinationTemperature(data: any): Promise<void>;

    setMaxPower(data: any): Promise<void>;

    setPowerTemperatureCorrelation(data: any): Promise<void>;

}