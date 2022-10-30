export interface PowerEndpoint {

    powerOff(): Promise<void>;
    restart(): Promise<void>;
    restartBrewery(): Promise<void>;

}