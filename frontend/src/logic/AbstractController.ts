import {Screen} from '../components/Screen';

export abstract class AbstractController {

    protected screen: Screen;

    protected abstract setListeners(): void;

    handleError<T>( promise: Promise<T> ): Promise<T> {
        return promise.then( value => {
            this.screen.hideError();
            return value;
        }).catch(e => {
            this.screen.showError('Błąd HTTP', e.message);
            return null;
        });
    }

    abstract start(): void;

}