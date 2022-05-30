function isValidNumber(text) {
    return text.match(/\d*\.?\d*/)[0] != ""
}

initNumeric = function(propName) {
    var componentName = "#brewing-" + propName;

    $(componentName).keypress(function(event) {
        var charCode = event.originalEvent.charCode;


        return true;
    });
}

attachActionToSwitch = function(name, url) {
    var componentName = '#brewing-' + name;

    $(componentName).change(function(event) {
        $.post(url, JSON.stringify({
            "enable": $(componentName).prop("checked")
        }));
    });
}

attachActionNumeric = function(name, fieldName, url) {
    var componentName = '#brewing-' + name;

    var $component = $(componentName);

    var doAction = function() {
        if ($component[0].validity.valid) {
            $.post(url, JSON.stringify({
                [fieldName]: $component.val()
            }));
        } else {
            $component.val("");
        }
    }

    $(componentName)
        .blur(function(event) {
            doAction();
        })
        .keypress(function(event) {
            if (event.originalEvent.key == 'Enter') {
                doAction();
            }
        })
}

initInterface = function() {
    $.ajaxSetup({
        contentType: "application/json; charset=utf-8",
        dataType: 'json'
    });

    attachActionToSwitch("enabled", "/brewing/enable");
    attachActionToSwitch("temperatureAlarm", "/brewing/enableTemperatureAlarm");
    attachActionToSwitch("motorEnabled", "/brewing/motorEnable");

    attachActionNumeric("destinationTemperature", "temperature", "/brewing/setDestinationTemperature");
    attachActionNumeric("maxPower", "power", "/brewing/setMaxPower");
    attachActionNumeric(
        "powerTemperatureCorrelation", "powerTemperatureCorrelation", "/brewing/setPowerTemperatureCorrelation"
    );
}

function updateValueIfNotFocused(data, propName) {
    var componentName = "#brewing-" + propName;
    if (!$(componentName).is(":focus")) {
        $(componentName).val(data[propName]);
    }
}

function updateSwitchIfNotFocused(data, propName) {
    var componentName = "#brewing-" + propName;
    if (!$(componentName).is(":focus")) {
        $(componentName).prop("checked", data[propName]);
    }
}

updateInterface = function(data) {
    $("#brewing-currentTemperature").val(data.currentTemperature);
    $("#brewing-heatingPower").val(data.heatingPower);

    updateSwitchIfNotFocused(data, "enabled");
    updateSwitchIfNotFocused(data, "temperatureAlarm");
    updateSwitchIfNotFocused(data, "motorEnabled");

    updateValueIfNotFocused(data, "destinationTemperature");
    updateValueIfNotFocused(data, "maxPower");
    updateValueIfNotFocused(data, "powerTemperatureCorrelation");
}

hideError = function() {
    $("#brewing-errors").hide();
}

showError = function(message) {
    $("#brewing-errors > .content").text(message);
    $("#brewing-errors").show();
}