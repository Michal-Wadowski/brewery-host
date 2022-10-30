import {FakeBreweryEndpoint} from './brewery/FakeBreweryEndpoint'
import {RealBreweryEndpoint} from './brewery/RealBreweryEndpoint'
import {BreweryEndpoint} from './brewery/BreweryEndpoint'

import {FakeConfigurationEndpoint} from './configuration/FakeConfigurationEndpoint'
import {RealConfigurationEndpoint} from './configuration/RealConfigurationEndpoint'
import {ConfigurationEndpoint} from './configuration/ConfigurationEndpoint'

import {FakePowerEndpoint} from './power/FakePowerEndpoint'
import {RealPowerEndpoint} from './power/RealPowerEndpoint'
import {PowerEndpoint} from './power/PowerEndpoint'

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
            return new RealConfigurationEndpoint();
        } else {
            return new FakeConfigurationEndpoint();
        }
    }

    static createPowerEndpoint(): PowerEndpoint {
        if (process.env.NODE_ENV == 'production') {
            return new RealPowerEndpoint();
        } else {
            return new FakePowerEndpoint();
        }
    }
}