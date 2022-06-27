import "./styles.css";
import 'bootstrap/dist/css/bootstrap.min.css';

import {BrewingController} from './logic/BrewingController'

console.log("environment: " + process.env.NODE_ENV)

let brewingController = new BrewingController();
brewingController.start();

