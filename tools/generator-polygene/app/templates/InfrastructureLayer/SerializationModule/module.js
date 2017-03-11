module.exports = {

    write: function (p) {
        p.copyTemplate(p.ctx,
            'InfrastructureLayer/SerializationModule/bootstrap.tmpl',
            'bootstrap/src/main/java/' + p.javaPackageDir + '/bootstrap/infrastructure/' + p.serialization + 'ValueSerializationModule.java');
    }
};
