import $ from "jquery";

import {Screen} from '../components/Screen';
import {NumberInput} from "../components/NumberInput";
import {Checkbox} from "../components/Checkbox";

import {BreweryEndpointFactory} from '../api/BreweryEndpointFactory'
import {BreweryEndpoint} from '../api/BreweryEndpoint'
import {BrewingState} from '../api/dto/BrewingState'

export class BrewingController {

    private screen: Screen;

    private breweryEndpoint: BreweryEndpoint;
    private currentTemperature: NumberInput;
    private heatingPower: NumberInput;
    private enabled: Checkbox;
    private temperatureAlarm: Checkbox;
    private motorEnabled: Checkbox;
    private destinationTemperature: NumberInput;
    private maxPower: NumberInput;
    private powerTemperatureCorrelation: NumberInput;

    constructor() {
        this.breweryEndpoint = BreweryEndpointFactory.createBreweryEndpoint();

        this.screen = new Screen("Brewery Application", "brewing");

        this.currentTemperature = new NumberInput($("#currentTemperature"))
        this.heatingPower = new NumberInput($("#heatingPower"))

        this.enabled = new Checkbox($("#enabled"))
        this.temperatureAlarm = new Checkbox($("#temperatureAlarm"))
        this.motorEnabled = new Checkbox($("#motorEnabled"))
        this.destinationTemperature = new NumberInput($("#destinationTemperature"))
        this.maxPower = new NumberInput($("#maxPower"))
        this.powerTemperatureCorrelation = new NumberInput($("#powerTemperatureCorrelation"))

        this.setListeners();

        this.screen.init();
    }

    private setListeners(): void {
        this.enabled.onChange((value: boolean) => {
            this.handleError(this.breweryEndpoint.enable({enable: value}));
        });

        this.temperatureAlarm.onChange((value: boolean) => {
            this.handleError(this.breweryEndpoint.enableTemperatureAlarm({enable: value}));
        });

        this.motorEnabled.onChange((value: boolean) => {
            this.handleError(this.breweryEndpoint.motorEnable( {enable: value}));
        });

        this.destinationTemperature.onChange((value: number) => {
            this.handleError(this.breweryEndpoint.setDestinationTemperature({temperature: value}));
        });

        this.maxPower.onChange((value: number) => {
            this.handleError(this.breweryEndpoint.setMaxPower({power: value}));
        });

        this.powerTemperatureCorrelation.onChange((value: number) => {
            this.handleError(
                this.breweryEndpoint.setPowerTemperatureCorrelation({powerTemperatureCorrelation: value})
            );
        });
    }

    handleError<T>( promise: Promise<T> ): Promise<T> {
        return promise.then( value => {
            this.screen.hideError();
            return value;
        }).catch(e => {
            this.screen.showError('Błąd HTTP', e.message);
            return null;
        });
    }

    start(): void {
        setInterval(() => {
            this.handleError(this.breweryEndpoint.getBrewingState()).then((brewingState: BrewingState) => {
                if( brewingState == null) {
                    return;
                }
                console.log(brewingState);

                this.currentTemperature.setValue(brewingState.currentTemperature);

                this.heatingPower.setValue(brewingState.heatingPower);

                this.enabled.setValue(brewingState.enabled);

                this.temperatureAlarm.setValue(brewingState.temperatureAlarm);

                this.motorEnabled.setValue(brewingState.motorEnabled);

                this.destinationTemperature.setValue(brewingState.destinationTemperature);

                this.maxPower.setValue(brewingState.maxPower);

                this.powerTemperatureCorrelation.setValue(brewingState.powerTemperatureCorrelation);
            });

        }, 1000);
    }

}