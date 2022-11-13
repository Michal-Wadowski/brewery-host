import {AbstractEndpoint} from '../AbstractEndpoint'

export class PowerEndpoint extends AbstractEndpoint {

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