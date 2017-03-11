
module.exports = {

    write: function (p) {
        p.copyTemplate(p.ctx,
            'DomainLayer/bootstrap.tmpl',
            'bootstrap/src/main/java/' + p.javaPackageDir + '/bootstrap/domain/DomainLayer.java');
        p.copyModules(__dirname );
    }
};
