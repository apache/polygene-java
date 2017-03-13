module.exports = {

    write: function (p) {
        copyLayer("Configuration");
        copyLayer("Infrastructure");
        copyLayer("Domain");
        copyLayer("UserInterface");
    }
};

function copyLayer(layerName) {
    var layer = require(__dirname + '/../' + layerName + 'Layer/layer.js');
    layer.write(polygene);
}
