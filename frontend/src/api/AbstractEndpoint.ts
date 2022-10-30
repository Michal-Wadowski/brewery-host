import _ from 'lodash'
import $ from 'jquery'


export class AbstractEndpoint {

    constructor() {
        $.ajaxSetup({
            contentType: "application/json; charset=utf-8",
            dataType: 'text'
        });
    }

    private handleFail(reject: any) {
        return (jqXHR: any) => {
            console.log({jqXHR});
            reject(new Error(
                "status: " + jqXHR.status + "\n" +
                "statusText: " + jqXHR.statusText + "\n" +
                "responseText:\n\n" + jqXHR.statusText
            ));
        }
    }

    private handleResult(resolve: any, rawResult: any) {
        resolve(_.size(rawResult) >= 2 ? JSON.parse(rawResult) : null);
    }

    protected getRequest<T>(url: string): Promise<T> {
        return new Promise<T>( (resolve, reject) => {
            $.get('/api' + url, (rawResult) => {
                this.handleResult(resolve, rawResult);
            }).fail(this.handleFail(reject));
        });
    }

    protected postRequest<T>(url: string, data: any): Promise<T> {
        return new Promise<T>( (resolve, reject) => {
            $.post('/api' + url, JSON.stringify(data), (rawResult) => {
                this.handleResult(resolve, rawResult);
            }).fail(this.handleFail(reject));
        });
    }

}