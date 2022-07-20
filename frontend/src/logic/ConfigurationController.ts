import $ from "jquery";
import _ from "lodash";

import {AbstractController} from './AbstractController'
import {Screen} from '../components/Screen';
import {NumberInput} from "../components/NumberInput";
import {Checkbox} from "../components/Checkbox";

import {EndpointFactory} from '../api/EndpointFactory'
import {ConfigurationEndpoint} from '../api/configuration/ConfigurationEndpoint'
import {TemperatureSensor} from '../api/configuration/dto/TemperatureSensor'
import {SensorsConfiguration} from '../api/configuration/dto/SensorsConfiguration'
import {ShowSensorDto} from '../api/configuration/dto/ShowSensorDto'
import {UseSensorDto} from '../api/configuration/dto/UseSensorDto'

export class ConfigurationController extends AbstractController {

    private configurationEndpoint: ConfigurationEndpoint;

    constructor() {
        super()

        this.configurationEndpoint = EndpointFactory.createConfigurationEndpoint();

        this.screen = new Screen("Konfiguracja browaru", "configuration");

        this.screen.init();
    }

    override start(): void {
        setInterval(() => {
            this.handleError(Promise.all([
                this.configurationEndpoint.getTemperatureSensors(),
                this.configurationEndpoint.getSensorsConfiguration()
            ])).then((result) => {
                console.log(result);
                if (result != null) {
                    this.updateTemperatureSensors(result[0], result[1]);
                }
            });
        }, 1000);
    }

    private updateTemperatureSensors(
        temperatureSensors: Array<TemperatureSensor>,
        sensorsConfiguration: SensorsConfiguration
    ): void {
        let $tbody = $("#sensors-configuration > tbody");
        $tbody.empty();
        temperatureSensors.forEach( (sensor) => {
            let $row = $('<tr>');
            $row.append('<td>' + sensor.sensorId + '</td>');
            $row.append('<td>' + sensor.temperature + '</td>');

            let $showCheckbox = $('<input class="form-check-input" type="checkbox">');

            let showCheckbox = new Checkbox($showCheckbox);
            showCheckbox.onChange((value: boolean) => {
                this.handleError(this.configurationEndpoint.showSensor({
                    show: value,
                    sensorId: sensor.sensorId
                } as ShowSensorDto)).then();
            });
            showCheckbox.setValue(this.isIdOnList(sensor.sensorId, sensorsConfiguration.showBrewingSensorIds));

            let $showCheckboxCell = $('<td>');
            $showCheckboxCell.append($showCheckbox);
            $row.append($showCheckboxCell);

            let $useCheckbox = $('<input class="form-check-input" type="checkbox">');

            let useCheckbox = new Checkbox($useCheckbox);
            useCheckbox.onChange((value: boolean) => {
                this.handleError(this.configurationEndpoint.useSensor({
                    use: value,
                    sensorId: sensor.sensorId
                } as UseSensorDto)).then();
            });
            useCheckbox.setValue(this.isIdOnList(sensor.sensorId, sensorsConfiguration.useBrewingSensorIds));

            let $useCheckboxCell = $('<td>');
            $useCheckboxCell.append($useCheckbox);
            $row.append($useCheckboxCell);

            $tbody.append($row);
        } );

        let averageShow = this.getAverageTemperature(temperatureSensors, sensorsConfiguration.showBrewingSensorIds);
        let averageUse = this.getAverageTemperature(temperatureSensors, sensorsConfiguration.useBrewingSensorIds);

        let $tfoot = $("#sensors-configuration > tfoot");
        $tfoot.empty();
        let $footRow = $('<tr>');
        $footRow.append('<th scope="row" colspan="2">Średnia</th>');
        $footRow.append('<td>' + _.defaultTo(averageShow, '-') + '</td>');
        $footRow.append('<td>' + _.defaultTo(averageUse, '-') + '</td>');
        $tfoot.append($footRow)
    }

    getAverageTemperature(
       temperatureSensors: Array<TemperatureSensor>,
       sensorIds: Array<string>
    ): number {
        let filteredSensors = _.filter(temperatureSensors,
            (sensor) => this.isIdOnList(sensor.sensorId, sensorIds)
        );

        let sum = _.reduce(filteredSensors, (sum, sensor) => sum + sensor.temperature, 0);
        if (filteredSensors.length > 0) {
            return sum / filteredSensors.length;
        } else {
            return null;
        }
    }

    isIdOnList(id: string, idList: Array<string>): boolean {
        return _.indexOf(idList, id) != -1;
    }
}