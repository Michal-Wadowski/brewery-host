import {PowerEndpoint} from './PowerEndpoint'

export class FakePowerEndpoint implements PowerEndpoint {

    powerOff(): Promise<void> {
        return new Promise((resolve) => {
            console.log("powerOff")
            resolve()
        });
    }

    restart(): Promise<void> {
        return new Promise((resolve) => {
            console.log("restart")
            resolve()
        });
    }

    restartBrewery(): Promise<void> {
        return new Promise((resolve) => {
            console.log("restartBrewery")
            resolve()
        });
    }
}