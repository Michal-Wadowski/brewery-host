import {AbstractEndpoint} from '../AbstractEndpoint'
import {PowerEndpoint} from './PowerEndpoint'

export class RealPowerEndpoint extends AbstractEndpoint implements PowerEndpoint {

    powerOff(): Promise<void> {
        return this.postRequest<void>("/power/powerOff", null);
    }

    restart(): Promise<void> {
        return this.postRequest<void>("/power/restart", null);
    }

    restartBrewery(): Promise<void> {
        return this.postRequest<void>("/power/restartBrewery", null);
    }

}