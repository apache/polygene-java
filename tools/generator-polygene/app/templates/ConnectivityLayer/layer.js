
module.exports = {

    write: function (p) {
        p.copyTemplate(p.ctx,
            'ConnectivityLayer/bootstrap.tmpl',
            'bootstrap/src/main/java/' + p.javaPackageDir + '/bootstrap/connectivity/ConnectivityLayer.java');
        p.copyModules(__dirname );
    }
};
