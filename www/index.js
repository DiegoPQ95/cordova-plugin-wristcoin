var exec = require('cordova/exec');

var WristCoin = {
    // Función para invocar la funcionalidad nativa de Android/iOS
    initializeDevice: function (successCallback, errorCallback) {
        // Llamada a la función nativa en el dispositivo (a través de exec)
        exec(successCallback, errorCallback, 'WristCoin', 'initializeDevice', []);
    }
    , readWristBand: function (successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'WristCoin', 'readWristBand', []);
    }
};

module.exports = WristCoin;
