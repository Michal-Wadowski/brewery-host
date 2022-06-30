import {FakeBreweryEndpoint} from './brewery/FakeBreweryEndpoint'
import {RealBreweryEndpoint} from './brewery/RealBreweryEndpoint'
import {BreweryEndpoint} from './brewery/BreweryEndpoint'

import {FakeConfigurationEndpoint} from './configuration/FakeConfigurationEndpoint'
import {ConfigurationEndpoint} from './configuration/ConfigurationEndpoint'

export class EndpointFactory {

    static createBreweryEndpoint(): BreweryEndpoint {
        if (process.env.NODE_ENV == 'production') {
            return new RealBreweryEndpoint();
        } else {
            return new FakeBreweryEndpoint();
        }
    }

    static createConfigurationEndpoint(): ConfigurationEndpoint {
        if (process.env.NODE_ENV == 'production') {
            throw new Error('Not implemented');
        } else {
            return new FakeConfigurationEndpoint();
        }
    }

}