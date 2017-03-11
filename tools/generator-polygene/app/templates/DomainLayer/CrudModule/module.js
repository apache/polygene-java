
module.exports = {

    write: function (p) {
        p.copyTemplate(p.ctx,
            'DomainLayer/CrudModule/bootstrap.tmpl',
            'bootstrap/src/main/java/' + p.javaPackageDir + '/bootstrap/domain/CrudModule.java');
    }
};
