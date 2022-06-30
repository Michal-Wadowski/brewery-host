import {BrewingState} from './dto/BrewingState'
import {BreweryEndpoint} from './BreweryEndpoint'
import {TemperatureSensor} from '../configuration/dto/TemperatureSensor'

import _ from 'lodash'

export class FakeBreweryEndpoint implements BreweryEndpoint {

    private brewingState: BrewingState = {
        enabled: true,
        currentTemperature: [
            {sensorId: "foo-123", temperature: 36.6} as TemperatureSensor,
            {sensorId: "bar-987", temperature: 70.3} as TemperatureSensor,
            {sensorId: "#use", temperature: 53.45} as TemperatureSensor
        ],
        destinationTemperature: 90.0,
        maxPower: 70,
        powerTemperatureCorrelation: 1.0,
        motorEnabled: true,
        temperatureAlarm: true,
        heatingPower: 70
    } as BrewingState

    getBrewingState(): Promise<BrewingState> {
        return new Promise<BrewingState>((resolve, reject) => {
//             if( Math.random() < 0.3 ) {
//                 reject(new Error(
//                     "status: test\n" +
//                     "statusText: test status\n" +
//                     "responseText:\n\n<h1>Test status</h1>"
//                 ));
//             }
            resolve(this.brewingState)
        });
    }

    enable(data: any): Promise<void> {
        return new Promise<void>((resolve, reject) => {
            if (!this.hasEnabled(data)) {
                throw new Error("Invalid data for enable endpoint");
            }
            this.brewingState.enabled = data['enable'];
            resolve()
        });
    }

    enableTemperatureAlarm(data: any): Promise<void> {
        return new Promise<void>((resolve, reject) => {
            if (!this.hasEnabled(data)) {
                throw new Error("Invalid data for enableTemperatureAlarm endpoint");
            }
            this.brewingState.temperatureAlarm = data['enable'];
            resolve()
        });
    }

    motorEnable(data: any): Promise<void> {
        return new Promise<void>((resolve, reject) => {
            if (!this.hasEnabled(data)) {
                throw new Error("Invalid data for motorEnable endpoint");
            }
            this.brewingState.motorEnabled = data['enable'];
            resolve()
        });
    }

    setDestinationTemperature(data: any): Promise<void> {
        return new Promise<void>((resolve, reject) => {
            if (!this.hasNumber(data, 'temperature')) {
                throw new Error("Invalid data for setDestinationTemperature endpoint");
            }
            this.brewingState.destinationTemperature = data['temperature'];
            resolve()
        });
    }

    setMaxPower(data: any): Promise<void> {
        return new Promise<void>((resolve, reject) => {
            if (!this.hasNumber(data, 'power')) {
                throw new Error("Invalid data for setMaxPower endpoint");
            }
            this.brewingState.maxPower = data['power'];
            resolve()
        });
    }

    setPowerTemperatureCorrelation(data: any): Promise<void> {
        return new Promise<void>((resolve, reject) => {
            if (!this.hasNumber(data, 'powerTemperatureCorrelation')) {
                throw new Error("Invalid data for setPowerTemperatureCorrelation endpoint");
            }
            this.brewingState.powerTemperatureCorrelation = data['powerTemperatureCorrelation'];
            resolve()
        });
    }

    private hasEnabled(data: any): boolean {
        return _.has(data, 'enable') && _.isBoolean(data['enable']);
    }

    private hasNumber(data: any, name: string): boolean {
        return _.has(data, name) && (_.isNumber(data[name]) || _.isNull(data[name]))
    }
}