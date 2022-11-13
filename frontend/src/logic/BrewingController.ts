import $ from "jquery";
import _ from "lodash";

import {AbstractController} from './AbstractController'
import {Screen} from '../components/Screen';
import {NumberInput} from "../components/NumberInput";
import {Checkbox} from "../components/Checkbox";

import {BreweryEndpoint} from '../api/brewery/BreweryEndpoint'
import {BrewingState} from '../api/brewery/dto/BrewingState'
import {TemperatureSensor} from '../api/configuration/dto/TemperatureSensor'

export class BrewingController extends AbstractController {

    private breweryEndpoint: BreweryEndpoint;
    private heatingPower: NumberInput;
    private enabled: Checkbox;
    private temperatureAlarm: Checkbox;
    private motorEnabled: Checkbox;
    private destinationTemperature: NumberInput;
    private maxPower: NumberInput;
    private powerTemperatureCorrelation: NumberInput;

    constructor() {
        super()
        this.breweryEndpoint = new BreweryEndpoint();

        this.screen = new Screen("Browar", "brewing");

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

    protected setListeners(): void {
        this.enabled.onChange((value: boolean) => {
            this.handleError(this.breweryEndpoint.enable({enable: value})).then();
        });

        this.temperatureAlarm.onChange((value: boolean) => {
            this.handleError(this.breweryEndpoint.enableTemperatureAlarm({enable: value})).then();
        });

        this.motorEnabled.onChange((value: boolean) => {
            this.handleError(this.breweryEndpoint.motorEnable( {enable: value})).then();
        });

        this.destinationTemperature.onChange((value: number) => {
            this.handleError(this.breweryEndpoint.setDestinationTemperature({temperature: value})).then();
        });

        this.maxPower.onChange((value: number) => {
            this.handleError(this.breweryEndpoint.setMaxPower({power: value})).then();
        });

        this.powerTemperatureCorrelation.onChange((value: number) => {
            this.handleError(
                this.breweryEndpoint.setPowerTemperatureCorrelation({powerTemperatureCorrelation: value}).then()
            );
        });
    }

    override start(): void {
        setInterval(() => {
            this.handleError(this.breweryEndpoint.getBrewingState()).then((brewingSnapshotState: BrewingState) => {
                if( brewingSnapshotState == null) {
                    return;
                }

                this.showCurrentTemperature(brewingSnapshotState.readings.currentTemperature)

                this.heatingPower.setValue(brewingSnapshotState.readings.heatingPower);

                this.enabled.setValue(brewingSnapshotState.settings.enabled);

                this.temperatureAlarm.setValue(brewingSnapshotState.settings.temperatureAlarmEnabled);

                this.motorEnabled.setValue(brewingSnapshotState.settings.motorEnabled);

                this.destinationTemperature.setValue(brewingSnapshotState.settings.destinationTemperature);

                this.maxPower.setValue(brewingSnapshotState.settings.maxPower);

                this.powerTemperatureCorrelation.setValue(brewingSnapshotState.settings.powerTemperatureCorrelation);
            });

        }, 1000);
    }

    showCurrentTemperature(temperatureSensors: Array<TemperatureSensor>): void {
        let $placeholder = $('#currentTemperature-placeholder')

        $placeholder.empty();

        if (_.size(temperatureSensors) > 0) {
            temperatureSensors.forEach( (sensor) => {
                let $item = $('<div class="form-floating col-md-4 mb-3"/>');
                let $input = $('<input class="form-control" disabled="disabled">');

                if (sensor.used) {
                    $input.css({border: "3px green solid"});
                }

                let sensorInputId = 'currentTemperature-' + sensor.sensorId;
                $input.attr('id', sensorInputId);
                $input.val(sensor.temperature);
                $item.append($input);
                let $label = $('<label/>');
                $label.attr('for', sensorInputId);

                if (sensor.sensorId != '#used') {
                    $label.text(sensor.name ? sensor.name : sensor.sensorId);
                } else {
                    $label.text("Średnia");
                    $item.addClass('used');
                }
                $item.append($label);
                $placeholder.append($item)
            });
        } else {
            $placeholder.append('<p class="alert alert-danger">najpierw skonfiguruj</p>');
        }
    }

}