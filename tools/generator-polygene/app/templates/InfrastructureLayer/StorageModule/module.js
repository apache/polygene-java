module.exports = {

    write: function (p) {
        p.copyTemplate(p.ctx,
            'InfrastructureLayer/StorageModule/bootstrap.tmpl',
            'bootstrap/src/main/java/' + p.javaPackageDir + '/bootstrap/infrastructure/' + p.entitystore + 'StorageModule.java');
    }
};
