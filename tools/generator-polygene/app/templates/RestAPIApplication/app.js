
module.exports = {

    write: function (p) {
        copyLayer(p, "Configuration");
        copyLayer(p, "Infrastructure");
        copyLayer(p, "Domain");
        copyLayer(p, "Connectivity");

        p.copyTemplate(p.ctx,
            'RestAPIApplication/web.xml.tmpl',
            'app/src/main/webapp/WEB-INF/web.xml');
    }
};

function copyLayer( p, layerName ) {
    var layer = require(__dirname + '/../' + layerName + 'Layer/layer.js');
    layer.write(p);
}
