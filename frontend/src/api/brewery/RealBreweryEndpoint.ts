import {BrewingState} from './dto/BrewingState'
import {BreweryEndpoint} from './BreweryEndpoint'
import _ from 'lodash'
import $ from 'jquery'

import {AbstractEndpoint} from '../AbstractEndpoint'

export class RealBreweryEndpoint extends AbstractEndpoint implements BreweryEndpoint {

    getBrewingState(): Promise<BrewingState> {
        return this.getRequest<BrewingState>("/brewing/getBrewingState");
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