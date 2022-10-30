import {ValueComponent} from './ValueComponent'

export class Checkbox implements ValueComponent<boolean> {

    private $checkbox: JQuery<HTMLElement>;
    private onChangeCallback: (data: boolean) => void;

    constructor($checkbox: JQuery<HTMLElement>) {
        if( !$checkbox.length ) {
            throw new Error("JQuery checkbox doesn't exists");
        }

        this.$checkbox = $checkbox;

        this.$checkbox.change(() => {
            if (this.onChangeCallback != null) {
                this.onChangeCallback(this.$checkbox.prop("checked"));
            }
        });
    }

    onChange(callback: (data: boolean) => void): void {
        this.onChangeCallback = callback;
    }

    setValue(value: boolean): void {
        if (!this.$checkbox.is(":focus")) {
            this.$checkbox.prop("checked", value);
        }
    }

    getValue(): boolean {
        return this.$checkbox.prop("checked");
    }
}