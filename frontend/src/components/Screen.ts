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

        $('#loading').remove();
        $('#' + this.id + " > .main-content").show();
    }

    showError(title: string, error: string): void {
        if ($("#" + this.id + "-errors").length == 0) {
            $('#' + this.id).prepend(`
                <div id="` + this.id + `-errors" class="mb-3 alert alert-danger error-dialog">
                   <h3 class="alert-headingr">` + title + `</h3>
                   <p class="content"></p>
                </div>`);
        }

        $("#" + this.id + "-errors > .content").text(error);
    }

    hideError(): void {
        $("#" + this.id + "-errors").remove();
    }
}