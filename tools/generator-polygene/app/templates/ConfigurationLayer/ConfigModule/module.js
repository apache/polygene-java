module.exports = {

    write: function (p) {
        p.copyTemplate(p.ctx,
            'ConfigurationLayer/ConfigModule/bootstrap.tmpl',
            'bootstrap/src/main/java/' + p.javaPackageDir + '/bootstrap/config/ConfigModule.java');
    }
};
