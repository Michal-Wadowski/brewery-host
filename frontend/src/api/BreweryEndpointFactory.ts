import {FakeBreweryEndpoint} from '../api/FakeBreweryEndpoint'
import {RealBreweryEndpoint} from '../api/RealBreweryEndpoint'
import {BreweryEndpoint} from '../api/BreweryEndpoint'

export class BreweryEndpointFactory {
    static createBreweryEndpoint(): BreweryEndpoint {
        if (process.env.NODE_ENV == 'production') {
            return new RealBreweryEndpoint();
        } else {
            return new FakeBreweryEndpoint();
        }
    }
}