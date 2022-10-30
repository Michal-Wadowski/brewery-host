import {ValueComponent} from './ValueComponent'
import _ from 'lodash'

export class NumberInput implements ValueComponent<number> {

    private $numberInput: JQuery<HTMLInputElement>;
    private onChangeCallback: (data: number) => void;

    constructor($numberInput: JQuery<HTMLElement>) {
        if( !$numberInput.length ) {
            throw new Error("JQuery numberInput doesn't exists");
        }

        this.$numberInput = $numberInput as JQuery<HTMLInputElement>;

        let onChange = () => {
            if (this.$numberInput[0].validity.valid) {
                let textVal = this.$numberInput.val();
                if (_.isEmpty(textVal)) {
                   this.onChangeCallback(null);
                } else {
                   this.onChangeCallback(_.toNumber(textVal));
                }
            } else {
                this.$numberInput.val("");
            }
        }

        this.$numberInput
            .blur((event) => {
                onChange();
            })
            .keypress((event) => {
                if (event.originalEvent.key == 'Enter') {
                    onChange();
                }
            })
    }

    onChange(callback: (data: number) => void): void {
        this.onChangeCallback = callback;
    }

    setValue(value: number): void {
        if (!this.$numberInput.is(":focus")) {
            this.$numberInput.val(value)
        }
    }

    getValue(): number {
        return this.$numberInput.val() as number;
    }
}