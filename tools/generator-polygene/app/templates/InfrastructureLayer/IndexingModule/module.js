module.exports = {

    write: function (p) {
        p.copyTemplate(p.ctx,
            'InfrastructureLayer/IndexingModule/bootstrap.tmpl',
            'bootstrap/src/main/java/' + p.javaPackageDir + '/bootstrap/infrastructure/' + p.indexing + 'IndexingModule.java');
    }
};
