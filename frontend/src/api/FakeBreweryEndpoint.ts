import {BrewingState} from './dto/BrewingState'
import {BreweryEndpoint} from './BreweryEndpoint'
import _ from 'lodash'

export class FakeBreweryEndpoint implements BreweryEndpoint {

    private brewingState: BrewingState;

    constructor() {
        this.brewingState = new BrewingState();
        this.brewingState.enabled = true;
        this.brewingState.currentTemperature = 50.0;
        this.brewingState.destinationTemperature = 90.0;
        this.brewingState.maxPower = 70;
        this.brewingState.powerTemperatureCorrelation = 1.0;
        this.brewingState.motorEnabled = true;
        this.brewingState.temperatureAlarm = true;
        this.brewingState.heatingPower = 70;
    }

    getBrewingState(): Promise<BrewingState> {
        return new Promise<BrewingState>((resolve) => {
            if( Math.random() < 0.3 ) {
                throw new Error("error");
            }
            resolve(this.brewingState)
        });
    }

    enable(data: any): Promise<void> {
        return new Promise<void>((resolve, reject) => {
            if (!this.hasEnabled(data)) {
                throw new Error("Invalid data for enable endpoint");
            }
            this.brewingState.enabled = data['enable'];
        });
    }

    enableTemperatureAlarm(data: any): Promise<void> {
        return new Promise<void>((resolve, reject) => {
            if (!this.hasEnabled(data)) {
                throw new Error("Invalid data for enableTemperatureAlarm endpoint");
            }
            this.brewingState.temperatureAlarm = data['enable'];
        });
    }

    motorEnable(data: any): Promise<void> {
        return new Promise<void>((resolve, reject) => {
            if (!this.hasEnabled(data)) {
                throw new Error("Invalid data for motorEnable endpoint");
            }
            this.brewingState.motorEnabled = data['enable'];
        });
    }

    setDestinationTemperature(data: any): Promise<void> {
        return new Promise<void>((resolve, reject) => {
            if (!this.hasNumber(data, 'temperature')) {
                throw new Error("Invalid data for setDestinationTemperature endpoint");
            }
            this.brewingState.destinationTemperature = data['temperature'];
        });
    }

    setMaxPower(data: any): Promise<void> {
        return new Promise<void>((resolve, reject) => {
            if (!this.hasNumber(data, 'power')) {
                throw new Error("Invalid data for setMaxPower endpoint");
            }
            this.brewingState.maxPower = data['power'];
        });
    }

    setPowerTemperatureCorrelation(data: any): Promise<void> {
        return new Promise<void>((resolve, reject) => {
            if (!this.hasNumber(data, 'powerTemperatureCorrelation')) {
                throw new Error("Invalid data for setPowerTemperatureCorrelation endpoint");
            }
            this.brewingState.powerTemperatureCorrelation = data['powerTemperatureCorrelation'];
        });
    }

    private hasEnabled(data: any): boolean {
        return _.has(data, 'enable') && _.isBoolean(data['enable']);
    }

    private hasNumber(data: any, name: string): boolean {
        return _.has(data, name) && (_.isNumber(data[name]) || _.isNull(data[name]))
    }
}