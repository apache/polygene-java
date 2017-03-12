
module.exports = {

    write: function (p) {
        if( p.applicationtype === 'Rest API' ) {
            p.copyTemplate(p.ctx,
                'DomainLayer/CrudModule/bootstrap.tmpl',
                'bootstrap/src/main/java/' + p.javaPackageDir + '/bootstrap/domain/CrudModule.java');
        }
    }
};
