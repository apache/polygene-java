
module.exports = {

    write: function (p) {
        p.copyTemplate(p.ctx,
            'ConfigurationLayer/bootstrap.tmpl',
            'bootstrap/src/main/java/' + p.javaPackageDir + '/bootstrap/config/ConfigurationLayer.java');
        p.copyModules(__dirname );
    }
};
