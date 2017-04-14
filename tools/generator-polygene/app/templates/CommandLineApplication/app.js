module.exports = {

    write: function (p) {
        copyLayer(p, "Configuration");
        copyLayer(p, "Infrastructure");
        copyLayer(p, "Domain");
        copyLayer(p, "UserInterface");




    }
};

function copyLayer(p, layerName) {
    var layer = require(__dirname + '/../' + layerName + 'Layer/layer.js');
    layer.write(p);
}
