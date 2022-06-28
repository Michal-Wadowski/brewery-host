import {FakeBreweryEndpoint} from './brewery/FakeBreweryEndpoint'
import {RealBreweryEndpoint} from './brewery/RealBreweryEndpoint'
import {BreweryEndpoint} from './brewery/BreweryEndpoint'

export class EndpointFactory {
    static createBreweryEndpoint(): BreweryEndpoint {
        if (process.env.NODE_ENV == 'production') {
            return new RealBreweryEndpoint();
        } else {
            return new FakeBreweryEndpoint();
        }
    }
}