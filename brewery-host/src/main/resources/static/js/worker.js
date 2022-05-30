worker = function() {
    $.get("/brewing/getBrewingState", function(data) {
        hideError();

        updateInterface(data);

        setTimeout(worker, 1000);
    }).fail(function(jqXHR, textStatus) {
        showError(textStatus);

        setTimeout(worker, 5000);
    });
}
