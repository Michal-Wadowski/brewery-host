import {BrewingState} from './dto/BrewingState'
import {BreweryEndpoint} from './BreweryEndpoint'
import _ from 'lodash'
import $ from 'jquery'

export class RealBreweryEndpoint implements BreweryEndpoint {

    constructor() {
        $.ajaxSetup({
            contentType: "application/json; charset=utf-8",
            dataType: 'json'
        });
    }

    getBrewingState(): Promise<BrewingState> {
        return this.getRequest<BrewingState>("/brewing/getBrewingState");
    }

    private handleFail(reject: any) {
        return (jqXHR: any) => {
            reject(new Error(
                "status: " + jqXHR.status + "\n" +
                "statusText: " + jqXHR.statusText + "\n" +
                "responseText:\n\n" + jqXHR.statusText
            ));
        }
    }

    private getRequest<T>(url: string): Promise<T> {
        return new Promise<T>( (resolve, reject) => {
            $.get(url, (result) => {
                resolve(result);
            }).fail(this.handleFail(reject));
        });
    }

    private postRequest<T>(url: string, data: any): Promise<T> {
        return new Promise<T>( (resolve, reject) => {
            $.post(url, JSON.stringify(data), (result) => {
                resolve(result);
            }).fail(this.handleFail(reject));
        });
    }

    enable(data: any): Promise<void> {
        return this.postRequest<void>("/brewing/enable", data);
    }

    enableTemperatureAlarm(data: any): Promise<void> {
        return this.postRequest<void>("/brewing/enableTemperatureAlarm", data);
    }

    motorEnable(data: any): Promise<void> {
        return this.postRequest<void>("/brewing/motorEnable", data);
    }

    setDestinationTemperature(data: any): Promise<void> {
        return this.postRequest<void>("/brewing/setDestinationTemperature", data);
    }

    setMaxPower(data: any): Promise<void> {
        return this.postRequest<void>("/brewing/setMaxPower", data);
    }

    setPowerTemperatureCorrelation(data: any): Promise<void> {
        return this.postRequest<void>("/brewing/setPowerTemperatureCorrelation", data);
    }

}