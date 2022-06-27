import $ from "jquery";

export class Screen {

    private title: string;
    private id: string;

    constructor(title: string, id: string) {
        this.title = title;
        this.id = id;
    }

    init(): void {
        $("head").append(
            $('<title>').text(this.title)
        );

        $('#' + this.id).show();
    }

    showError(title: string, error: string): void {
        if ($("#brewing-errors").length == 0) {
            $('#brewing').prepend(`<div id="brewing-errors" class="mb-3 alert alert-danger error-dialog">
                   <h3 class="alert-headingr">` + title + `</h3>
                   <p class="content"></p>
            </div>`);
        }

        $("#brewing-errors > .content").text(error);
    }

    hideError(): void {
        $("#brewing-errors").remove();
    }
}