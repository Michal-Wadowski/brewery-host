import {Screen} from '../components/Screen';

export abstract class AbstractController {

    protected screen: Screen;

    handleError<T>( promise: Promise<T> ): Promise<T> {
        return promise.then( value => {
            this.screen.hideError();
            return value;
        }).catch(e => {
            console.log('AbstractController.handleError()', {e});
            this.screen.showError('Błąd HTTP', e.message);
            return null;
        });
    }

    abstract start(): void;

}