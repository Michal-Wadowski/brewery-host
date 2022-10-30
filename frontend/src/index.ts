import "./styles.css";
import 'bootstrap/dist/css/bootstrap.min.css';

import 'bootstrap';

import {BrewingController} from './logic/BrewingController';
import {ConfigurationController} from './logic/ConfigurationController';

console.log("environment: " + process.env.NODE_ENV);

window.globals = {BrewingController, ConfigurationController};

